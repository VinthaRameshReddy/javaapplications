package com.medgo.member.components;

import com.medgo.jwt.JwtTokenUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.HttpServletRequest;

@Slf4j
@Component
@RequiredArgsConstructor
@SuppressWarnings({"Duplicates", "unused"})
public class MedGoCustomSecurityContext {

    private final JwtTokenUtil jwtTokenUtil;

    @PostConstruct
    public void init() {
        log.info("MedGo MedGoCustomSecurityContext initialized for membership-service");
    }

    /**
     * Extract JWT token from request header
     * Works for both GET and POST methods
     */
    private String getTokenFromRequest() {
        return getTokenFromRequest(null);
    }
    
    /**
     * Extract JWT token from request header - with explicit HttpServletRequest parameter
     * This method is more reliable for GET methods
     */
    public String getTokenFromRequest(HttpServletRequest request) {
        try {
            // Use provided request if available, otherwise try RequestContextHolder
            HttpServletRequest httpRequest = request;
            if (httpRequest == null) {
                ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
                if (attributes != null) {
                    httpRequest = attributes.getRequest();
                }
            }
            
            if (httpRequest != null) {
                // Try standard Authorization header
                String bearerToken = httpRequest.getHeader("Authorization");
                if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
                    String token = bearerToken.substring(7);
                    log.debug("Extracted token from Authorization header");
                    return token;
                }
                // Try alternative header names (case-insensitive)
                bearerToken = httpRequest.getHeader("authorization");
                if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
                    return bearerToken.substring(7);
                }
                bearerToken = httpRequest.getHeader("AUTHORIZATION");
                if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
                    return bearerToken.substring(7);
                }
                
                log.warn("Authorization header not found in request. Available headers: {}", 
                        java.util.Collections.list(httpRequest.getHeaderNames()));
            } else {
                log.warn("HttpServletRequest is null. RequestContextHolder attributes: {}", 
                        RequestContextHolder.getRequestAttributes() != null ? "available" : "null");
            }
        } catch (Exception e) {
            log.error("Error extracting token from request: {}", e.getMessage(), e);
        }
        return null;
    }

    public String authenticatedToken() {
        log.debug("@authenticatedToken");
        String token = getTokenFromRequest();
        if (token != null) {
            return "Bearer %s".formatted(token);
        }
        return null;
    }

    public String authenticatedUsername() {
        return authenticatedUsername(null);
    }
    
    public String authenticatedUsername(HttpServletRequest request) {
        log.debug("@authenticatedUsername");
        String token = getTokenFromRequest(request);
        if (token != null) {
            try {
                // First try to get username from claim
                String username = jwtTokenUtil.getUsernameFromTokenClaim(token);
                if (username != null && !username.isEmpty()) {
                    return username;
                }
                // Fallback to subject (deviceId)
                return jwtTokenUtil.getUsernameFromToken(token);
            } catch (Exception e) {
                log.error("Error extracting username from token: {}", e.getMessage());
            }
        }
        return null;
    }

    public String authenticatedMemberCode() {
        return authenticatedMemberCode(null);
    }
    
    public String authenticatedMemberCode(HttpServletRequest request) {
        log.debug("@authenticatedMemberCode");
        String token = getTokenFromRequest(request);
        if (token != null) {
            try {
                return jwtTokenUtil.getMemberCodeFromTokenClaim(token);
            } catch (Exception e) {
                log.error("Error extracting memberCode from token: {}", e.getMessage());
            }
        }
        return null;
    }

    public String authenticatedDeviceId() {
        log.debug("@authenticatedDeviceId");
        String token = getTokenFromRequest();
        if (token != null) {
            try {
                // DeviceId is stored as subject
                return jwtTokenUtil.getUsernameFromToken(token);
            } catch (Exception e) {
                log.error("Error extracting deviceId from token: {}", e.getMessage());
            }
        }
        return null;
    }
}

