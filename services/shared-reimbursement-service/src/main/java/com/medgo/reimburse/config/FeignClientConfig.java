package com.medgo.reimburse.config;

import com.medgo.reimburse.service.AzureKeyVaultService;
import feign.Logger;
import feign.RequestInterceptor;
import feign.Retryer;
import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.Base64;
import java.util.List;
import java.util.concurrent.TimeUnit;


@Slf4j
@Configuration
@DependsOn("azureKeyVaultService")
public class FeignClientConfig {

    private final AzureKeyVaultService keyVaultService;

    // Basic Auth credentials (bcrypt password) - same username and password as SecurityConfig
    private String feignUser;
    private String feignPassword;
    private String apimHeader;
    private String apimKey;

    public FeignClientConfig(AzureKeyVaultService keyVaultService) {
        this.keyVaultService = keyVaultService;
    }

    @PostConstruct
    public void init() {
        loadFeignSecrets();
        log.info("=== FeignClientConfig Initialization Complete ===");

        if (isEmpty(feignUser) || isEmpty(feignPassword)) {
            throw new IllegalStateException("Failed to initialize FeignClientConfig: Basic Auth credentials missing from Azure Key Vault");
        } else {
            log.info("FeignClientConfig initialized successfully (Basic Auth + API Key)");
        }
        log.info("================================================");
    }

    private void loadFeignSecrets() {
        try {
            this.feignUser = sanitize(keyVaultService.getSecretValue("app-security-user"));
            this.feignPassword = sanitize(keyVaultService.getSecretValue("app-security-password"));
            this.apimHeader = sanitize(keyVaultService.getSecretValue("app-security-api-key-header"));
            this.apimKey = sanitize(keyVaultService.getSecretValue("app-security-api-key"));

            // Validate required secrets
            if (isEmpty(feignUser)) {
                throw new IllegalStateException("app-security-user secret is missing or empty in Azure Key Vault");
            }
            if (isEmpty(feignPassword)) {
                throw new IllegalStateException("app-security-password-bcrypt secret is missing or empty in Azure Key Vault");
            }


            if (feignPassword.startsWith("{bcrypt}")) {
                this.feignPassword = feignPassword.substring(8); // Remove "{bcrypt}" prefix
                log.debug("Removed '{bcrypt}' prefix from password for Feign Basic Auth");
            }

            log.info("Successfully loaded Feign secrets from Azure Key Vault");

        } catch (IllegalStateException e) {
            log.error("Failed to load Feign secrets from Azure Key Vault: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Failed to load Feign secrets from Azure Key Vault: {}", e.getMessage(), e);
            throw new IllegalStateException("Failed to initialize FeignClientConfig: " + e.getMessage(), e);
        }
    }

    private String sanitize(String input) {
        return input == null ? null : input.replaceAll("[\\t\\n\\r]", "").trim();
    }

    private boolean isEmpty(String val) {
        return val == null || val.isBlank();
    }

    @Bean
    public Retryer feignRetryer() {
        return new Retryer.Default(1000, TimeUnit.SECONDS.toMillis(5), 6);
    }

    @Bean
    public Logger.Level feignLoggerLevel() {
        return Logger.Level.FULL;
    }

    @Bean
    @org.springframework.core.annotation.Order(1)
    public RequestInterceptor headerPropagationInterceptor() {
        return requestTemplate -> requestTemplate.header("Postman-Token", "bypass-encryption");
    }

    @Bean
    @org.springframework.core.annotation.Order(2)
    public RequestInterceptor feignBasicAuthInterceptor() {
        return requestTemplate -> {
            if (isEmpty(feignUser) || isEmpty(feignPassword)) {
                log.error("Feign Basic Auth credentials are empty at request time!");
                log.error("   User: '{}', Password: '{}'", feignUser, feignPassword != null ? "***" : "null");
                return;
            }

            // Add Basic Auth header with bcrypt password (same as SecurityConfig uses for validation)
            String auth = feignUser + ":" + feignPassword;
            String encoded = Base64.getEncoder().encodeToString(auth.getBytes());
            requestTemplate.header("Authorization", "Basic " + encoded);
            log.debug("Added Feign Basic Auth header for user: {}", feignUser);
        };
    }

    @Bean
    @org.springframework.core.annotation.Order(3)
    public RequestInterceptor apimSubscriptionKeyInterceptor() {
        return requestTemplate -> {
            if (!isEmpty(apimKey) && !isEmpty(apimHeader)) {
                requestTemplate.header(apimHeader, apimKey);
                log.debug("Added API key header: {}", apimHeader);
            } else {
                log.debug("Skipping API key header - credentials not available");
            }
        };
    }

    @Bean(name = "RequestInterceptor")
    @org.springframework.core.annotation.Order(4)
    public RequestInterceptor requestInterceptor() {
        return requestTemplate -> {
            ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attrs == null) return;

            HttpServletRequest req = attrs.getRequest();
            List<String> headers = List.of("X-DEVICE-ID", "X-APPLICATION-ID", "X-CLIENT-ID", "X-PLATFORM", "userId");

            for (String h : headers) {
                String v = req.getHeader(h);
                if (v != null) requestTemplate.header(h, v);
            }

            String authorization = req.getHeader("Authorization");
            if (authorization != null && !authorization.isBlank()) {
                String requestUrl = requestTemplate.url();
                boolean isNotificationService = requestUrl != null &&
                        (requestUrl.contains("/notification/") || requestUrl.contains(":10120"));

                if (authorization.startsWith("Bearer ")) {
                    if (isNotificationService) {
                        log.debug("Skipping JWT Authorization header for notification service (uses Basic Auth)");
                    } else {
                        requestTemplate.header("Authorization", authorization);
                        log.debug("Passed JWT Authorization header to Feign request");
                    }
                } else {
                    requestTemplate.header("Authorization", authorization);
                    log.debug("Passed non-JWT Authorization header to Feign request");
                }
            }
            String userId = req.getHeader("userId");
            if (userId == null || userId.isBlank()) {
                userId = extractUserIdFromToken(req);
                if (userId != null && !userId.isBlank()) {
                    requestTemplate.header("userId", userId);
                    log.debug("Extracted and added userId header from JWT token: {}", userId);
                }
            }
        };
    }

    private String extractUserIdFromToken(HttpServletRequest request) {
        try {
            String bearerToken = request.getHeader("Authorization");
            if (bearerToken == null || !bearerToken.startsWith("Bearer ")) {
                return null;
            }

            String token = bearerToken.substring(7);

            try {
                String[] parts = token.split("\\.");
                if (parts.length != 3) {
                    return null;
                }

                String payload = parts[1];
                while (payload.length() % 4 != 0) {
                    payload += "=";
                }

                byte[] decodedBytes = java.util.Base64.getUrlDecoder().decode(payload);
                String payloadJson = new String(decodedBytes);

                if (payloadJson.contains("\"username\"")) {
                    int usernameStart = payloadJson.indexOf("\"username\"") + 11;
                    int usernameEnd = payloadJson.indexOf("\"", usernameStart);
                    if (usernameEnd > usernameStart) {
                        return payloadJson.substring(usernameStart, usernameEnd);
                    }
                }

                if (payloadJson.contains("\"sub\"")) {
                    int subStart = payloadJson.indexOf("\"sub\"") + 6;
                    int subEnd = payloadJson.indexOf("\"", subStart);
                    if (subEnd > subStart) {
                        return payloadJson.substring(subStart, subEnd);
                    }
                }
            } catch (Exception jwtEx) {
                log.debug("Error parsing JWT token for userId: {}", jwtEx.getMessage());
            }

            return null;
        } catch (Exception e) {
            log.debug("Error extracting userId from token: {}", e.getMessage());
            return null;
        }
    }
}
