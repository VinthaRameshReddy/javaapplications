package com.medgo.loaservice.config;

import com.medgo.loaservice.service.TokenService;
import feign.Logger;
import feign.RequestInterceptor;
import feign.Response;
import feign.Retryer;
import feign.codec.ErrorDecoder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeUnit;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class FeignClientConfig {

    private final TokenService tokenService;

    @Value("${loa.apim-key:}")
    private String loaApimKey;

    @Bean(name = "defaultFeignRetryer")
    @Primary
    public Retryer feignRetryer() {
        return new Retryer.Default(1000, TimeUnit.SECONDS.toMillis(5), 6);
    }

    @Bean
    public Logger.Level feignLoggerLevel() {
        return Logger.Level.BASIC; // Changed from HEADERS to BASIC to avoid logging Authorization header and sensitive data
    }


    @Bean
    public ErrorDecoder errorDecoder() {
        return new ErrorDecoder() {
            private final ErrorDecoder defaultDecoder = new ErrorDecoder.Default();

            @Override
            public Exception decode(String methodKey, Response response) {
                int status = response.status();

                if (status == 401) {
                    log.error("401 Unauthorized - URL: {}, Method: {}", response.request().url(), methodKey);
                    if (response.body() != null) {
                        try {
                            byte[] bodyBytes = new byte[(int) response.body().length()];
                            response.body().asInputStream().read(bodyBytes);
                            String body = new String(bodyBytes, StandardCharsets.UTF_8);
                            log.error("Response: {}", body);
                        } catch (Exception e) {
                            log.error("Failed to read response body: {}", e.getMessage());
                        }
                    }
                    tokenService.clearToken();
                } else if (status == 403) {
                    log.error("403 Forbidden - URL: {}, Method: {}", response.request().url(), methodKey);
                    if (response.body() != null) {
                        try {
                            byte[] bodyBytes = new byte[(int) response.body().length()];
                            response.body().asInputStream().read(bodyBytes);
                            String body = new String(bodyBytes, StandardCharsets.UTF_8);
                            log.error("Response: {}", body);
                        } catch (Exception e) {
                            log.error("Failed to read response body: {}", e.getMessage());
                        }
                    }
                    tokenService.clearToken();
                } else if (status == 409) {
                    if (response.body() != null) {
                        try {
                            byte[] bodyBytes = new byte[(int) response.body().length()];
                            response.body().asInputStream().read(bodyBytes);
                            String body = new String(bodyBytes, StandardCharsets.UTF_8);
                            log.warn("409 Conflict - {}", body);
                        } catch (Exception e) {
                            log.warn("Failed to read response body: {}", e.getMessage());
                        }
                    }
                }

                return defaultDecoder.decode(methodKey, response);
            }
        };
    }


    @Bean
    public RequestInterceptor combinedHeadersInterceptor() {
        return requestTemplate -> {
            String requestUrl = requestTemplate.url();

            if (requestUrl != null && requestUrl.contains("/oauth2/token")) {
                return;
            }

            if (loaApimKey != null && !loaApimKey.isEmpty()) {
                requestTemplate.header("Ocp-Apim-Subscription-Key", loaApimKey);
            }

            if (requestTemplate.headers().containsKey("Content-Type")) {
                requestTemplate.headers().remove("Content-Type");
            }

            if (requestTemplate.body() != null && requestTemplate.body().length > 0
                    && "POST".equalsIgnoreCase(requestTemplate.method())) {
                requestTemplate.header("Content-Type", "application/json");
            }

            try {
                if (tokenService != null) {
                    String accessToken = tokenService.getAccessToken();
                    if (accessToken != null && !accessToken.isEmpty()) {
                        String tokenValue = accessToken.startsWith("Bearer ")
                                ? accessToken.substring(7)
                                : accessToken;
                        requestTemplate.header("Authorization", "Bearer " + tokenValue);
                    } else {
                        log.error("Access token is null or empty");
                    }
                }
            } catch (Exception e) {
                log.error("Failed to add Authorization header: {}", e.getMessage(), e);
                try {
                    tokenService.clearToken();
                } catch (Exception ignored) {
                }
            }
        };
    }
}

