package com.medgo.provider.config;

import com.medgo.provider.service.AzureKeyVaultService;
import feign.Logger;
import feign.RequestInterceptor;
import feign.Retryer;
import feign.auth.BasicAuthRequestInterceptor;
import jakarta.annotation.PostConstruct;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;

import java.util.concurrent.TimeUnit;

@Configuration
@DependsOn("azureKeyVaultService")
public class FeignClientConfig {

    private final AzureKeyVaultService keyVaultService;

    private String basicUser;
    private String basicPassword;
    private String apimHeader;
    private String apimKey;

    public FeignClientConfig(AzureKeyVaultService keyVaultService) {
        this.keyVaultService = keyVaultService;
    }

    @PostConstruct
    public void init() {
        loadSecrets();
    }

    private void loadSecrets() {
        try {
            this.basicUser = sanitize(keyVaultService.getSecretValue("shared-provider-basic-user"));
            this.basicPassword = sanitize(keyVaultService.getSecretValue("shared-provider-basic-password"));
            this.apimHeader = sanitize(keyVaultService.getSecretValue("shared-provider-apim-header"));
            this.apimKey = sanitize(keyVaultService.getSecretValue("shared-provider-apim-key"));
        } catch (Exception e) {
            // Failed to load secrets from Azure Key Vault
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

    /**
     * Remove userId header FIRST (before any other interceptors)
     * This must run BEFORE BasicAuthRequestInterceptor to prevent userId from being sent to shared services
     */
    @Bean
    @org.springframework.core.annotation.Order(1)
    public RequestInterceptor removeUserIdHeaderInterceptor() {
        return requestTemplate -> {
            // Remove userId in ALL possible case variations
            requestTemplate.removeHeader("userId");
            requestTemplate.removeHeader("UserId");
            requestTemplate.removeHeader("USERID");
            requestTemplate.removeHeader("userid");
            requestTemplate.removeHeader("UserID");
            
            // Remove JWT Authorization header if present
            java.util.Collection<String> authHeaders = requestTemplate.headers().get("Authorization");
            if (authHeaders != null) {
                for (String authHeader : new java.util.ArrayList<>(authHeaders)) {
                    if (authHeader != null && authHeader.startsWith("Bearer ")) {
                        requestTemplate.removeHeader("Authorization");
                        break;
                    }
                }
            }
        };
    }

    @Bean
    @org.springframework.core.annotation.Order(2)
    public RequestInterceptor headerPropagationInterceptor() {
        return requestTemplate -> requestTemplate.header("Postman-Token", "bypass-encryption");
    }

    @Bean
    @org.springframework.core.annotation.Order(3)
    public RequestInterceptor basicAuthRequestInterceptor() {
        return new BasicAuthRequestInterceptor(basicUser, basicPassword);
    }

    @Bean
    @org.springframework.core.annotation.Order(4)
    public RequestInterceptor apimSubscriptionKeyInterceptor() {
        return requestTemplate -> {
            if (!isEmpty(apimKey) && !isEmpty(apimHeader)) {
                requestTemplate.header(apimHeader, apimKey);
            }
        };
    }
}
