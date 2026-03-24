package com.medgo.auth.config;

import com.medgo.auth.service.AzureKeyVaultService;
import feign.Logger;
import feign.Request;
import feign.RequestInterceptor;
import feign.Retryer;
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
    private String apimHeader, apimKey;

    public FeignClientConfig(AzureKeyVaultService keyVaultService) {
        this.keyVaultService = keyVaultService;
    }

    @PostConstruct
    public void init() {
        loadSecrets();
    }

    private void loadSecrets() {
        try {
            this.apimHeader = sanitize(keyVaultService.getSecretValue("shared-provider-apim-header"));
            this.apimKey = sanitize(keyVaultService.getSecretValue("shared-provider-apim-key"));
            System.out.println(" Feign APIM secrets loaded from Azure Key Vault for auth-service");
        } catch (Exception e) {
            System.err.println(" Failed to load secrets from Azure Key Vault: " + e.getMessage());
        }
    }

    private String sanitize(String s) {
        return s == null ? null : s.replaceAll("[\\t\\n\\r]", "").trim();
    }

    private boolean isEmpty(String s) {
        return s == null || s.isBlank();
    }

    @Bean(name = "defaultFeignRetryer")
    @Primary
    public Retryer feignRetryer() {
        return new Retryer.Default(500, TimeUnit.SECONDS.toMillis(2), 3);
    }

    @Bean
    @Primary
    public Request.Options requestOptions() {
        return new Request.Options(
                5, TimeUnit.SECONDS,
                10, TimeUnit.SECONDS,
                true
        );
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
    public RequestInterceptor apimSubscriptionKeyInterceptor() {
        return t -> {
            if (!isEmpty(apimKey) && !isEmpty(apimHeader)) {
                t.header(apimHeader, apimKey);
            }
        };
    }

    @Bean(name = "customHeaderPropagationInterceptor")
    @org.springframework.core.annotation.Order(3)
    public RequestInterceptor customHeaderPropagationInterceptor() {
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