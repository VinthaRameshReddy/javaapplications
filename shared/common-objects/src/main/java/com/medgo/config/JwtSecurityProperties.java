package com.medgo.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Configuration properties for JWT security
 * 
 * Configure bypass URLs in application.properties using one of these formats:
 * 
 * Option 1 - Index notation (recommended for properties files):
 * jwt.security.bypass-urls[0]=/health
 * jwt.security.bypass-urls[1]=/actuator/health
 * jwt.security.bypass-urls[2]=/actuator/**
 * 
 * Option 2 - Comma-separated string (alternative):
 * jwt.security.bypass-urls=/health,/actuator/health,/actuator/**
 */
@Data
@Component
@ConfigurationProperties(prefix = "jwt.security")
public class JwtSecurityProperties {

    /**
     * List of URLs that should bypass JWT validation
     * Default includes common health check endpoints
     */
    private List<String> bypassUrls = new ArrayList<>();

    /**
     * Whether JWT security is enabled
     * Default: true
     */
    private boolean enabled = true;

    public JwtSecurityProperties() {
        // Default bypass URLs
        bypassUrls.add("/health");
        bypassUrls.add("/actuator/health");
        bypassUrls.add("/actuator/**");
    }

    /**
     * Setter that supports comma-separated string format
     * This allows using: jwt.security.bypass-urls=/health,/actuator/health
     */
    public void setBypassUrls(List<String> bypassUrls) {
        if (bypassUrls != null && !bypassUrls.isEmpty()) {
            // If the list contains a single comma-separated string, split it
            if (bypassUrls.size() == 1 && bypassUrls.get(0).contains(",")) {
                this.bypassUrls = Arrays.stream(bypassUrls.get(0).split(","))
                        .map(String::trim)
                        .filter(s -> !s.isEmpty())
                        .collect(Collectors.toList());
            } else {
                this.bypassUrls = bypassUrls;
            }
        } else {
            this.bypassUrls = new ArrayList<>();
        }
    }
}

