package com.medgo.crypto.aspect;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.medgo.crypto.annotation.DecryptBody;
import com.medgo.crypto.dto.EncryptedWrapper;
import com.medgo.crypto.service.EncryptionService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.MethodParameter;
import org.springframework.stereotype.Component;
import org.springframework.util.StreamUtils;
import org.springframework.util.StringUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;

@Component
@RequiredArgsConstructor
public class DecryptBodyArgumentResolver implements HandlerMethodArgumentResolver {
    private final Logger logger = LoggerFactory.getLogger(DecryptBodyArgumentResolver.class);

    private final ObjectMapper objectMapper;
    private final EncryptionService encryptionService;

    @Value("${encryption.enabled:true}")
    private boolean encryptionEnabled;

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return parameter.hasParameterAnnotation(DecryptBody.class);
    }

    @Override
    public Object resolveArgument(MethodParameter parameter,
            ModelAndViewContainer mavContainer,
            NativeWebRequest webRequest,
            WebDataBinderFactory binderFactory) throws Exception {

        HttpServletRequest request = webRequest.getNativeRequest(HttpServletRequest.class);

        // Check global + header toggle

        boolean skipDecryption = !encryptionEnabled;

        // Read body
        String body;
        if (request instanceof MultipartHttpServletRequest multipartRequest) {
            MultipartFile jsonPart = multipartRequest.getFile(parameter.getParameterName());
            body = (jsonPart != null) ? new String(jsonPart.getBytes(), StandardCharsets.UTF_8) : "";
        } else {
            body = StreamUtils.copyToString(request.getInputStream(), StandardCharsets.UTF_8);
        }

        //        disable for postman testing
        String postmanToken = request.getHeader("Postman-Token");
        if (StringUtils.hasText(postmanToken)) {
            logger.info("Postman-Token detected, skipping encryption");
            skipDecryption = true;
        }
//                ends here

        // Target type
        Class<?> targetType = parameter.getParameterAnnotation(DecryptBody.class).value();

        Object target;
        if (skipDecryption) {
            target = objectMapper.readValue(body, targetType);
        } else {
            EncryptedWrapper wrapper = objectMapper.readValue(body, EncryptedWrapper.class);
            target = encryptionService.decryptPayload(wrapper, targetType);
            logger.info("Decrypted payload: {}", target);
        }

        // Validation
        boolean shouldValidate = Arrays.stream(parameter.getParameterAnnotations())
                                       .anyMatch(a -> a.annotationType().equals(Valid.class) ||
                                               a.annotationType().equals(Validated.class));

        if (shouldValidate) {
            WebDataBinder binder = binderFactory.createBinder(webRequest, target, parameter.getParameterName());
            binder.validate();
            if (binder.getBindingResult().hasErrors()) {
                throw new MethodArgumentNotValidException(parameter, binder.getBindingResult());
            }
        }

        return target;
    }

}
