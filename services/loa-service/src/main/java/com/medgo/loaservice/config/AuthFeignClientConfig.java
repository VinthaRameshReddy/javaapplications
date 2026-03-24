package com.medgo.loaservice.config;

import feign.Logger;
import feign.RequestInterceptor;
import feign.Retryer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.concurrent.TimeUnit;


@Slf4j
@Configuration
public class AuthFeignClientConfig {

    @Value("${medicard.auth.basic.username}")
    private String basicAuthUsername;

    @Value("${medicard.auth.basic.password}")
    private String basicAuthPassword;

    @Value("${loa.apim-key}")
    private String loaApimKey;

    @Bean
    public Retryer feignRetryer() {
        return new Retryer.Default(1000, TimeUnit.SECONDS.toMillis(5),
                6);
    }

    @Bean
    public Logger.Level feignLoggerLevel() {
        return Logger.Level.BASIC; // Changed from FULL to BASIC to avoid logging Authorization header and credentials
    }


    @Bean
    public RequestInterceptor apimKeyInterceptor() {
        return requestTemplate -> {
            String requestUrl = requestTemplate.url();
            if (requestUrl != null && requestUrl.contains("/oauth2/token")) {
                if (loaApimKey != null && !loaApimKey.isEmpty()) {
                    requestTemplate.header("Ocp-Apim-Subscription-Key", loaApimKey);
                } else {
                    log.error("LOA APIM Key is not configured");
                }
            }
        };
    }

    @Bean
    public RequestInterceptor basicAuthInterceptor() {
        return requestTemplate -> {
            String requestUrl = requestTemplate.url();
            if (requestUrl != null && requestUrl.contains("/oauth2/token")) {
                String credentials = basicAuthUsername + ":" + basicAuthPassword;
                String encodedCredentials = Base64.getEncoder()
                        .encodeToString(credentials.getBytes(StandardCharsets.UTF_8));
                requestTemplate.header("Authorization", "Basic " + encodedCredentials);
            }
        };
    }
}
