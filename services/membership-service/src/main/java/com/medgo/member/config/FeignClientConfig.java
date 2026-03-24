package com.medgo.member.config;

import com.medgo.member.service.AzureKeyVaultService;
import feign.Logger;
import feign.RequestInterceptor;
import feign.Retryer;
import feign.auth.BasicAuthRequestInterceptor;
import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.context.annotation.Primary;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.List;
import java.util.concurrent.TimeUnit;

@Configuration
@DependsOn("azureKeyVaultService")
public class FeignClientConfig {

    private final AzureKeyVaultService keyVaultService;
    private String basicUser, basicPassword, apimHeader, apimKey;

    public FeignClientConfig(AzureKeyVaultService keyVaultService) {
        this.keyVaultService = keyVaultService;
    }

    @PostConstruct
    public void init() { loadSecrets(); }

    private void loadSecrets() {
        try {
            this.basicUser = sanitize(keyVaultService.getSecretValue("app-security-user"));
            this.basicPassword = sanitize(keyVaultService.getSecretValue("app-security-password-bcrypt"));
            this.apimHeader = sanitize(keyVaultService.getSecretValue("app-security-api-key-header"));
            this.apimKey = sanitize(keyVaultService.getSecretValue("app-security-api-key"));
        } catch (Exception e) {
            System.err.println("Failed to load secrets from Azure Key Vault: " + e.getMessage());
        }
    }

    private String sanitize(String s) { return s == null ? null : s.replaceAll("[\\t\\n\\r]", "").trim(); }
    private boolean isEmpty(String s) { return s == null || s.isBlank(); }

    // ONLY ONE @Primary
    @Bean(name = "defaultFeignRetryer")
    @Primary
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
        return t -> t.header("Postman-Token", "bypass-encryption");
    }

    @Bean
    @org.springframework.core.annotation.Order(2)
    public RequestInterceptor basicAuthRequestInterceptor() {
        return new BasicAuthRequestInterceptor(basicUser, basicPassword);
    }

    @Bean
    @org.springframework.core.annotation.Order(3)
    public RequestInterceptor apimSubscriptionKeyInterceptor() {
        return t -> {
            if (!isEmpty(apimKey) && !isEmpty(apimHeader)) {
                t.header(apimHeader, apimKey);
            }
        };
    }

    // NEVER copy Authorization
    @Bean(name = "memberRequestInterceptor")
    @org.springframework.core.annotation.Order(4)
    public RequestInterceptor memberRequestInterceptor() {
        return requestTemplate -> {
            ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attrs == null) return;

            HttpServletRequest req = attrs.getRequest();
            List<String> headers = List.of("X-DEVICE-ID", "X-APPLICATION-ID", "X-CLIENT-ID", "X-PLATFORM");

            for (String h : headers) {
                String v = req.getHeader(h);
                if (v != null) requestTemplate.header(h, v);
            }
        };
    }
}