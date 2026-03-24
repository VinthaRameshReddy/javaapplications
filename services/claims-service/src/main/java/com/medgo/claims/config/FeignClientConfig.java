package com.medgo.claims.config;


import com.medgo.claims.service.AzureKeyVaultService;
import com.medgo.jwt.JwtTokenUtil;
import feign.Logger;
import feign.RequestInterceptor;
import feign.Retryer;
import feign.auth.BasicAuthRequestInterceptor;
import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.List;
import java.util.concurrent.TimeUnit;

@Slf4j
@Configuration
@DependsOn("azureKeyVaultService")
public class FeignClientConfig {

    private final AzureKeyVaultService keyVaultService;
    private final JwtTokenUtil jwtTokenUtil;

    private String basicUser;
    private String basicPassword;
    private String apimHeader;
    private String apimKey;

    public FeignClientConfig(AzureKeyVaultService keyVaultService, JwtTokenUtil jwtTokenUtil) {
        this.keyVaultService = keyVaultService;
        this.jwtTokenUtil = jwtTokenUtil;
    }

    @PostConstruct
    public void init() {
        loadSecrets();
    }

    private void loadSecrets() {
        try {
            this.basicUser = sanitize(keyVaultService.getSecretValue("app-security-user"));
            this.basicPassword = sanitize(keyVaultService.getSecretValue("app-security-password-bcrypt"));
            this.apimHeader = sanitize(keyVaultService.getSecretValue("app-security-api-key-header"));
            this.apimKey = sanitize(keyVaultService.getSecretValue("app-security-api-key"));
        } catch (Exception e) {
            System.err.println("⚠️ Failed to load secrets from Azure Key Vault: " + e.getMessage());
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
    public RequestInterceptor headerPropagationInterceptor() {
        return requestTemplate -> requestTemplate.header("Postman-Token", "bypass-encryption");
    }

    @Bean
    public RequestInterceptor basicAuthRequestInterceptor() {
        return new BasicAuthRequestInterceptor(basicUser, basicPassword);
    }

    @Bean
    public RequestInterceptor apimSubscriptionKeyInterceptor() {
        return requestTemplate -> {
            if (!isEmpty(apimKey)) {
                requestTemplate.header(apimHeader, apimKey);
            }
        };
    }
    @Bean(name = "RequestInterceptor")
    @org.springframework.core.annotation.Order(4)
    public RequestInterceptor RequestInterceptor() {
        return requestTemplate -> {
            ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attrs == null) return;

            HttpServletRequest req = attrs.getRequest();
            List<String> headers = List.of("X-DEVICE-ID", "X-APPLICATION-ID", "X-CLIENT-ID", "X-PLATFORM");

            for (String h : headers) {
                String v = req.getHeader(h);
                if (v != null) requestTemplate.header(h, v);
            }

            String userId = extractUserIdFromToken(req);
            if (userId != null && !userId.isBlank()) {
                requestTemplate.header("userId", userId);
                log.debug("Extracted and added userId header from JWT token: {}", userId);
            } else {
                log.debug("Could not extract userId from JWT token for Feign request");
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

            String username = jwtTokenUtil.getUsernameFromTokenClaim(token);
            if (username != null && !username.isBlank()) {
                return username;
            }

            String subject = jwtTokenUtil.getUsernameFromToken(token);
            if (subject != null && !subject.isBlank()) {
                return subject;
            }
            
            return null;
        } catch (Exception e) {
            log.debug("Error extracting userId from token: {}", e.getMessage());
            return null;
        }
    }

    }



