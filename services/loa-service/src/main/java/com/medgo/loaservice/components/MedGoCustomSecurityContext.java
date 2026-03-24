package com.medgo.loaservice.components;

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
        log.info("MedGo MedGoCustomSecurityContext initialized for loa-service");
    }

    private String getTokenFromRequest() {
        return getTokenFromRequest(null);
    }

    public String getTokenFromRequest(HttpServletRequest request) {
        try {
            HttpServletRequest httpRequest = request;
            if (httpRequest == null) {
                ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
                if (attributes != null) {
                    httpRequest = attributes.getRequest();
                }
            }

            if (httpRequest != null) {
                String bearerToken = httpRequest.getHeader("Authorization");
                if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
                    String token = bearerToken.substring(7);
                    log.debug("Extracted token from Authorization header");
                    return token;
                }
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
                String username = jwtTokenUtil.getUsernameFromTokenClaim(token);
                if (username != null && !username.isEmpty()) {
                    return username;
                }
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
                return jwtTokenUtil.getUsernameFromToken(token);
            } catch (Exception e) {
                log.error("Error extracting deviceId from token: {}", e.getMessage());
            }
        }
        return null;
    }
}



