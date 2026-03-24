package com.medgo.reimburse.config;

import com.medgo.reimburse.service.AzureKeyVaultService;
import feign.RequestInterceptor;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;

import java.util.Base64;


@Slf4j
@Configuration
@DependsOn("azureKeyVaultService")
public class NotificationFeignConfig {

    private final AzureKeyVaultService keyVaultService;

    // Basic Auth credentials
    private String feignUser;
    private String feignPassword;
    private String apimHeader;
    private String apimKey;

    public NotificationFeignConfig(AzureKeyVaultService keyVaultService) {
        this.keyVaultService = keyVaultService;
    }

    @PostConstruct
    public void init() {
        loadFeignSecrets();
        if (isEmpty(feignUser) || isEmpty(feignPassword)) {
            throw new IllegalStateException("Failed to initialize NotificationFeignConfig: Basic Auth credentials missing from Azure Key Vault");
        }
    }

    private void loadFeignSecrets() {
        try {
            this.feignUser = sanitize(keyVaultService.getSecretValue("app-security-user"));
            this.feignPassword = sanitize(keyVaultService.getSecretValue("app-security-password"));
            this.apimHeader = sanitize(keyVaultService.getSecretValue("app-security-api-key-header"));
            this.apimKey = sanitize(keyVaultService.getSecretValue("app-security-api-key"));

            if (isEmpty(feignUser)) {
                throw new IllegalStateException("app-security-user secret is missing or empty in Azure Key Vault");
            }
            if (isEmpty(feignPassword)) {
                throw new IllegalStateException("app-security-password secret is missing or empty in Azure Key Vault");
            }

            if (feignPassword.startsWith("{bcrypt}")) {
                this.feignPassword = feignPassword.substring(8);
            }

        } catch (IllegalStateException e) {
            log.error("Failed to load Notification Feign secrets from Azure Key Vault: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Failed to load Notification Feign secrets from Azure Key Vault: {}", e.getMessage(), e);
            throw new IllegalStateException("Failed to initialize NotificationFeignConfig: " + e.getMessage(), e);
        }
    }

    private String sanitize(String input) {
        return input == null ? null : input.replaceAll("[\\t\\n\\r]", "").trim();
    }

    private boolean isEmpty(String val) {
        return val == null || val.isBlank();
    }


    @Bean
    @org.springframework.core.annotation.Order(1)
    public RequestInterceptor notificationHeaderPropagationInterceptor() {
        return requestTemplate -> requestTemplate.header("Postman-Token", "bypass-encryption");
    }

    @Bean
    @org.springframework.core.annotation.Order(2)
    public RequestInterceptor notificationBasicAuthInterceptor() {
        return requestTemplate -> {
            if (isEmpty(feignUser) || isEmpty(feignPassword)) {
                log.error("Notification Feign Basic Auth credentials are empty at request time");
                return;
            }

            String auth = feignUser + ":" + feignPassword;
            String encoded = Base64.getEncoder().encodeToString(auth.getBytes());
            requestTemplate.header("Authorization", "Basic " + encoded);
        };
    }

    @Bean
    @org.springframework.core.annotation.Order(3)
    public RequestInterceptor notificationApimSubscriptionKeyInterceptor() {
        return requestTemplate -> {
            if (!isEmpty(apimKey) && !isEmpty(apimHeader)) {
                requestTemplate.header(apimHeader, apimKey);
            }
        };
    }

    @Bean
    @org.springframework.core.annotation.Order(4)
    public RequestInterceptor notificationDeviceIdInterceptor() {
        return requestTemplate -> {
            String deviceId = com.medgo.reimburse.service.ReimbursementNotificationService.getDeviceIdFromContext();
            if (deviceId != null && !deviceId.isBlank()) {
                requestTemplate.header("X-DEVICE-ID", deviceId);
            } else {
                log.error("X-DEVICE-ID header not found - Notification service will reject the request");
            }

            String applicationId = com.medgo.reimburse.service.ReimbursementNotificationService.getApplicationIdFromContext();
            if (applicationId != null && !applicationId.isBlank()) {
                requestTemplate.header("X-APPLICATION-ID", applicationId);
            } else {
                log.error("X-APPLICATION-ID header not found - Notification service will reject the request");
            }
        };
    }

}

