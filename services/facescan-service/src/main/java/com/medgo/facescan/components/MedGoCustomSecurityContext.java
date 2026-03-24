package com.medgo.facescan.components;

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
        log.info("MedGo MedGoCustomSecurityContext initialized for facescan-service");
    }

    /**
     * Extract JWT token from request header
     */
    private String getTokenFromRequest() {
        try {
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attributes != null) {
                HttpServletRequest request = attributes.getRequest();
                if (request != null) {
                    String bearerToken = request.getHeader("Authorization");
                    if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
                        return bearerToken.substring(7);
                    }
                }
            }
        } catch (Exception e) {
            log.debug("Could not extract token from request: {}", e.getMessage());
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
        log.debug("@authenticatedUsername");
        String token = getTokenFromRequest();
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
        log.debug("@authenticatedMemberCode");
        String token = getTokenFromRequest();
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

