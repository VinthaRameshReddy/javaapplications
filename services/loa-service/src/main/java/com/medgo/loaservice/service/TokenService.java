package com.medgo.loaservice.service;

import com.medgo.loaservice.domain.dto.response.AuthResponseDTO;
import com.medgo.loaservice.feign.MedicardAuthClient;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;


@Slf4j
@Service
public class TokenService {

    @Value("${medicard.auth.username}")
    private String defaultUsername;

    @Value("${medicard.auth.password}")
    private String defaultPassword;

    @Value("${medicard.auth.grant-type}")
    private String grantType;

    private final MedicardAuthClient medicardAuthClient;


    @Autowired
    public TokenService(@Lazy MedicardAuthClient medicardAuthClient) {
        this.medicardAuthClient = medicardAuthClient;
    }

    private String cachedAccessToken;
    private LocalDateTime tokenExpiryTime;


    @PostConstruct
    public void initializeToken() {
        // Token will be fetched lazily on first getAccessToken() call
    }


    public String getAccessToken() {
        return getAccessToken(defaultUsername, defaultPassword);
    }


    public String getAccessToken(String username, String password) {
        // Check if cached token is still valid (with 5 minute buffer)
        if (cachedAccessToken != null && tokenExpiryTime != null) {
            LocalDateTime now = LocalDateTime.now();
            LocalDateTime bufferTime = tokenExpiryTime.minus(5, ChronoUnit.MINUTES);

            if (now.isBefore(bufferTime)) {
                return cachedAccessToken;
            }
        }

        try {
            MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
            formData.add("username", username);
            formData.add("password", password);
            formData.add("grant_type", grantType);

            AuthResponseDTO response = medicardAuthClient.getAccessToken(formData);

            if (response != null && response.getAccessToken() != null) {
                cachedAccessToken = response.getAccessToken();
                int expiresIn = response.getExpiresIn() != null ? response.getExpiresIn() : 300;
                tokenExpiryTime = LocalDateTime.now().plus(expiresIn, ChronoUnit.SECONDS);
                return cachedAccessToken;
            } else {
                log.error("Failed to obtain access token - response is null or missing access_token");
                throw new RuntimeException("Failed to obtain access token");
            }
        } catch (Exception e) {
            log.error("Error fetching access token: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to obtain access token: " + e.getMessage(), e);
        }
    }


    public void clearToken() {
        cachedAccessToken = null;
        tokenExpiryTime = null;
    }
}
