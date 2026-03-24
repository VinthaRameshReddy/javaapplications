package com.medgo.crypto.aspect;

import com.medgo.commons.CommonResponse;
import com.medgo.crypto.annotation.EncryptResponse;
import com.medgo.crypto.dto.EncryptedWrapper;
import com.medgo.crypto.service.EncryptionService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

@RestControllerAdvice
@RequiredArgsConstructor
public class EncryptResponseBodyAdvice implements ResponseBodyAdvice<Object> {
    private final Logger logger = LoggerFactory.getLogger(EncryptResponseBodyAdvice.class);

    private final EncryptionService encryptionService;
    @Value("${encryption.enabled:true}")
    private boolean encryptionEnabled;

    @Override
    public boolean supports(MethodParameter returnType, Class<? extends HttpMessageConverter<?>> converterType) {
        return encryptionEnabled &&
                (returnType.hasMethodAnnotation(EncryptResponse.class) ||
                        returnType.getContainingClass().isAnnotationPresent(EncryptResponse.class));
    }


    @Override
    public Object beforeBodyWrite(Object body,
            MethodParameter returnType,
            MediaType selectedContentType,
            Class<? extends HttpMessageConverter<?>> selectedConverterType,
            ServerHttpRequest request,
            ServerHttpResponse response) {

        if (body == null) return null;
//        disable for postman testing
        String postmanToken = request.getHeaders().getFirst("Postman-Token");
        if (StringUtils.hasText(postmanToken)) {
            logger.info("Postman-Token detected, skipping encryption");
            return body;
        }
//                ends here
        if (!encryptionEnabled) return body;

        // Skip if already encrypted
        if (body instanceof EncryptedWrapper) return body;

        // Case 2: CommonResponse → encrypt only the data field
        if (body instanceof CommonResponse commonResponse) {
            Object plainData = commonResponse.getData();
            EncryptedWrapper encrypted = null;
            try {
                encrypted = encryptionService.encryptPayload(plainData);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
            commonResponse.setData(encrypted);
            return commonResponse;
        }
        // Case 3: Any other object → wrap fully as encrypted (optional fallback)
        try {
            return encryptionService.encryptPayload(body);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}