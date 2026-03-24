package com.medgo.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.medgo.commons.RequestContext;
import com.medgo.commons.ErrorResponse;
import com.medgo.crypto.service.EncryptionService;
import com.medgo.enums.CustomStatusCode;
import com.medgo.jwt.JwtTokenUtil;
import com.medgo.jwt.JwtUserDetailsService;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.http.HttpServletResponseWrapper;

@Component
@Order(1)
@RequiredArgsConstructor
@Slf4j
public class JwtRequestFilter extends OncePerRequestFilter {

    private final EncryptionService encryptionService;
    private final JwtUserDetailsService jwtUserDetailsService;
    private final JwtTokenUtil jwtTokenUtil;
    private static final ObjectMapper mapper = new ObjectMapper();


    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain chain) throws ServletException, IOException {

        var requestTokenHeader = request.getHeader("Authorization");
        var requestURI = request.getRequestURI();
        var servletPath = request.getServletPath();
        log.info("requestURI : {}, servletPath: {}", requestURI, servletPath);

        response.setHeader("Access-Control-Allow-Origin", "*");
        response.setHeader("Access-Control-Allow-Methods", "*");
        response.setHeader("Access-Control-Allow-Headers", "*");
        response.setHeader("Access-Control-Expose-Headers", "X-Access-Token");

        // Check both requestURI (may include context path) and servletPath (context path stripped)
        if (enlistedForSecurityBypass(requestURI) || enlistedForSecurityBypass(servletPath)) {
            chain.doFilter(request, response);
            log.info("Bypassing JWT validation for endpoint: {} (servletPath: {})", requestURI, servletPath);
            return;
        }

        log.info("Validating JWT Token for endpoint: {}", requestURI);

        // Check userId header (which should contain the deviceId value)
        String headerUserId = request.getHeader("userId");
        if (headerUserId == null || headerUserId.isEmpty()) {
            headerUserId = request.getHeader("UserId");
        }
        if (headerUserId == null || headerUserId.isEmpty()) {
            headerUserId = request.getHeader("USERID");
        }
        if (headerUserId == null || headerUserId.isEmpty()) {
            headerUserId = request.getHeader("user-id");
        }

        // Log all headers for debugging
        log.info("Received headers - userId: {}, Authorization: {}",
                headerUserId != null ? headerUserId : "null",
                requestTokenHeader != null ? "present" : "null");

        if (!StringUtils.hasText(headerUserId)) {
            log.warn("Missing mandatory header: userId. Available headers: {}",
                    java.util.Collections.list(request.getHeaderNames()));
            writeErrorResponse(response, "Missing mandatory header: userId", 401, request);
            return;
        }

        if (requestTokenHeader == null || !requestTokenHeader.startsWith("Bearer ")) {
            // Token is missing or not in the correct format
            log.warn("Authorization header missing or invalid format. Header value: {}",
                    requestTokenHeader != null ? requestTokenHeader.substring(0, Math.min(20, requestTokenHeader.length())) : "null");
            writeErrorResponse(response, CustomStatusCode.AUTHORIZATION_FAILED.getMessage(), 401, request);
            return;
        }

            var jwtToken = requestTokenHeader.substring(7);
            
            // Reject if token has been blacklisted (explicit logout)
            if (jwtTokenUtil.isTokenBlacklisted(jwtToken)) {
                log.warn("JWT token is blacklisted (invalidated via logout): {}", jwtToken);
                writeErrorResponse(response, CustomStatusCode.INVALID_TOKEN.getMessage(), 401, request);
                return;
            }
        String tokenDeviceId;

        try {
            // Extract deviceId from token (token was generated with deviceId as subject)
            tokenDeviceId = jwtTokenUtil.getUsernameFromToken(jwtToken);
            log.info("Extracted deviceId from token: {}", tokenDeviceId);

            var userDetails = jwtUserDetailsService.loadUserByUsername(tokenDeviceId);
            log.info("Loaded userDetails for deviceId: {}", tokenDeviceId);

            // Validate token signature and expiration first
            boolean isTokenValid = jwtTokenUtil.validateToken(jwtToken, userDetails);
            log.info("Token validation result: {}, Token deviceId: {}, Header userId: {}",
                    isTokenValid, tokenDeviceId, headerUserId);

            // Check if userId from header matches deviceId from token (both should be the same)
            boolean deviceIdMatches = headerUserId.equals(tokenDeviceId);
            log.info("DeviceId match check: headerUserId={}, tokenDeviceId={}, matches={}",
                    headerUserId, tokenDeviceId, deviceIdMatches);

            if (isTokenValid && deviceIdMatches) {
                log.info("Token is valid for deviceId: {}. Setting authentication context.", tokenDeviceId);

                // Set authentication in SecurityContext
                if (SecurityContextHolder.getContext().getAuthentication() == null) {
                    var authToken = new UsernamePasswordAuthenticationToken(
                            userDetails, null, userDetails.getAuthorities());
                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                }

                // Generate refreshed token with username and memberCode from original token
                // Extract username and memberCode from the original token to include in refreshed token
                String tokenUsername = jwtTokenUtil.getUsernameFromTokenClaim(jwtToken);
                String tokenMemberCode = jwtTokenUtil.getMemberCodeFromTokenClaim(jwtToken);
                
                String accessToken;
                if (tokenUsername != null && !tokenUsername.isEmpty() && 
                    tokenMemberCode != null && !tokenMemberCode.isEmpty()) {
                    // Include username and memberCode in refreshed token
                    accessToken = jwtTokenUtil.generateToken(userDetails, tokenUsername, tokenMemberCode);
                    log.info("Generated refreshed token with username and memberCode: username={}, memberCode={}", 
                            tokenUsername, tokenMemberCode);
                } else if (tokenUsername != null && !tokenUsername.isEmpty()) {
                    // Include username only if memberCode is missing
                    accessToken = jwtTokenUtil.generateToken(userDetails, tokenUsername);
                    log.info("Generated refreshed token with username only: username={}", tokenUsername);
                } else {
                    // Fallback to basic token generation
                    accessToken = jwtTokenUtil.generateToken(userDetails);
                    log.warn("Generated refreshed token without username/memberCode claims");
                }
                response.addHeader("X-Access-Token", accessToken);

                // Wrap response to intercept and encrypt the response body
                var responseWrapper = new ResponseWrapper(response);
                chain.doFilter(request, responseWrapper);

                // Check if encryption should be skipped for testing (Postman-Token header)
                String postmanToken = request.getHeader("Postman-Token");
                boolean skipEncryption = StringUtils.hasText(postmanToken);

                if (skipEncryption) {
                    log.info("Postman-Token detected, skipping encryption for testing");
                }

                // Encrypt the response if it's not already encrypted and not skipping for testing
                String responseBody = responseWrapper.getResponseBody();
                if (responseBody != null && !responseBody.isEmpty() && !response.isCommitted()) {
                    try {
                        if (skipEncryption) {
                            // Skip encryption for testing - write plain response
                            response.getWriter().write(responseBody);
                        } else {
                            // Parse the response to check if it's already encrypted
                            try {
                                @SuppressWarnings("unchecked")
                                Map<String, Object> responseObj = mapper.readValue(responseBody, Map.class);

                                // Check if response is already in encrypted format
                                boolean isAlreadyEncrypted = false;
                                if (responseObj.containsKey("data") && responseObj.get("data") instanceof Map) {
                                    @SuppressWarnings("unchecked")
                                    Map<String, Object> dataMap = (Map<String, Object>) responseObj.get("data");
                                    if (dataMap.containsKey("encryptedData") || dataMap.containsKey("encryptedKey")) {
                                        // Already encrypted, write as-is
                                        isAlreadyEncrypted = true;
                                    }
                                }

                                if (isAlreadyEncrypted) {
                                    // Already encrypted, write as-is
                                    response.getWriter().write(responseBody);
                                } else {
                                    // Encrypt the response
                                    encryptAndWriteResponse(response, responseObj);
                                }
                            } catch (Exception e) {
                                log.warn("Failed to parse response as JSON, writing as-is: {}", e.getMessage());
                                if (!response.isCommitted()) {
                                    response.getWriter().write(responseBody);
                                }
                            }
                        }
                    } catch (Exception e) {
                        log.error("Error encrypting response: {}", e.getMessage(), e);
                        if (!response.isCommitted() && responseBody != null) {
                            try {
                                response.getWriter().write(responseBody);
                            } catch (IOException ioException) {
                                log.error("Failed to write response body: {}", ioException.getMessage());
                            }
                        }
                    }
                } else if (response.isCommitted()) {
                    log.warn("Response already committed, cannot encrypt response body");
                }
            } else {
                if (!isTokenValid) {
                    log.warn("Token validation failed - token invalid for deviceId: {}", tokenDeviceId);
                }
                if (!deviceIdMatches) {
                    log.warn("DeviceId mismatch - userId header: {}, token deviceId: {}", headerUserId, tokenDeviceId);
                }
                writeErrorResponse(response, CustomStatusCode.INVALID_TOKEN.getMessage(), 401, request);
                return;
            }
        } catch (ExpiredJwtException ex) {
            log.error("JWT token expired", ex);
            writeErrorResponse(response, CustomStatusCode.TOKEN_EXPIRED.getMessage(), 419, request);
            return;
        } catch (JwtException ex) {
            log.error("JWT signature validation failed", ex);
            writeErrorResponse(response, CustomStatusCode.INVALID_TOKEN.getMessage(), 401, request);
            return;
        } catch (Exception e) {
            log.error("Error validating JWT token", e);
            writeErrorResponse(response, "Error validating token: " + e.getMessage(), 401, request);
            return;
        }
    }

    private void writeErrorResponse(HttpServletResponse response, String message, int statusCode, HttpServletRequest request) {
        try {
            var traceId = RequestContext.getTraceId();

            // Check if encryption should be skipped for testing (Postman-Token header)
            String postmanToken = request != null ? request.getHeader("Postman-Token") : null;
            boolean skipEncryption = StringUtils.hasText(postmanToken);

            if (skipEncryption) {
                log.info("Postman-Token detected, skipping encryption for error response");
            }

            // Create ErrorResponse matching CommonResponse.error format
            // Map HTTP status code to internal status code (e.g., 401 -> 100)
            int internalStatusCode = mapHttpStatusToInternal(statusCode);
            String errorCode = getErrorCodeForStatus(statusCode, message);
            var errorResponse = new ErrorResponse(internalStatusCode, errorCode, message, List.of());

            var outerResponse = new LinkedHashMap<String, Object>();
            outerResponse.put("statusCode", "001");
            outerResponse.put("response", "ERROR");
            outerResponse.put("traceId", traceId);

            if (skipEncryption) {
                // Write plain error response for testing
                outerResponse.put("data", errorResponse);
            } else {
                // Encrypt the error payload
                var encrypted = encryptionService.encryptPayload(errorResponse);
                var data = Map.of(
                        "encryptedData", encrypted.encryptedData(),
                        "encryptedKey", encrypted.encryptedKey(),
                        "iv", encrypted.iv()
                );
                outerResponse.put("data", data);
            }

            response.setStatus(HttpServletResponse.SC_OK);
            response.setContentType("application/json");
            try (PrintWriter writer = response.getWriter()) {
                writer.write(mapper.writeValueAsString(outerResponse));
            }
        } catch (Exception e) {
            log.error("Error writing error response: {}", e.getMessage());
        }
    }

    private int mapHttpStatusToInternal(int httpStatusCode) {
        return switch (httpStatusCode) {
            case 401 -> 100;  // Unauthorized -> 100
            case 419 -> 101;  // Token Expired -> 101
            case 403 -> 102;  // Forbidden -> 102
            default -> httpStatusCode;
        };
    }

    private String getErrorCodeForStatus(int statusCode, String message) {
        return switch (statusCode) {
            case 401 -> "INVALID_JWT";
            case 419 -> "TOKEN_EXPIRED";
            case 403 -> "UNAUTHORIZED";
            default -> (message != null && message.toLowerCase().contains("otp")) ? "INVALID_OTP" : null;
        };
    }

    private String getOuterStatusCode(int statusCode) {
        return switch (statusCode) {
            case 401 -> "103";
            case 419 -> "104";
            case 403 -> "102";
            default -> String.format("%03d", statusCode);
        };
    }

    private void encryptAndWriteResponse(HttpServletResponse response, Map<String, Object> responseObj) {
        try {
            var traceId = RequestContext.getTraceId();

            // Extract data from response
            Object data = responseObj.get("data");
            if (data == null) {
                // If no data field, encrypt the entire response object
                data = responseObj;
            }

            // Encrypt the data
            var encrypted = encryptionService.encryptPayload(data);

            // Build encrypted response structure
            var encryptedData = Map.of(
                    "encryptedData", encrypted.encryptedData(),
                    "encryptedKey", encrypted.encryptedKey(),
                    "iv", encrypted.iv()
            );

            // Preserve original response structure but replace data with encrypted data
            var outerResponse = new LinkedHashMap<String, Object>();
            outerResponse.put("statusCode", responseObj.getOrDefault("statusCode", "000"));
            outerResponse.put("response", responseObj.getOrDefault("response", "SUCCESS"));
            outerResponse.put("data", encryptedData);
            if (traceId != null) {
                outerResponse.put("traceId", traceId);
            } else if (responseObj.containsKey("traceId")) {
                outerResponse.put("traceId", responseObj.get("traceId"));
            }

            response.setStatus(HttpServletResponse.SC_OK);
            response.setContentType("application/json");
            try (PrintWriter writer = response.getWriter()) {
                writer.write(mapper.writeValueAsString(outerResponse));
            }
        } catch (Exception e) {
            log.error("Error encrypting response: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to encrypt response", e);
        }
    }

    private static boolean enlistedForSecurityBypass(String requestURI) {
        if (requestURI == null || requestURI.isEmpty()) {
            return false;
        }



        // Remove query string if present for path matching
        String path = requestURI.contains("?") ? requestURI.substring(0, requestURI.indexOf("?")) : requestURI;
        // Normalize path (remove trailing slashes except root)
        path = path.endsWith("/") && path.length() > 1 ? path.substring(0, path.length() - 1) : path;

        // Check if this is an endpoint that requires JWT validation even under the auth-service path.
        // Currently `registerbio` and `logout` must be protected.
        String pathLower = path.toLowerCase();
        boolean isRegisterBio = pathLower.equals("/api/v1/auth/registerbio") ||
                               pathLower.equals("/auth-service/api/v1/auth/registerbio") ||
                               pathLower.endsWith("/api/v1/auth/registerbio");
        boolean isLogout = pathLower.equals("/api/v1/auth/logout") ||
                           pathLower.equals("/auth-service/api/v1/auth/logout") ||
                           pathLower.endsWith("/api/v1/auth/logout");

        if (isRegisterBio || isLogout) {
            log.info("Protected auth endpoint detected (registerbio/logout) - requiring JWT validation for path: {}", path);
            return false; // Do NOT bypass - require JWT validation
        }

        // Exception: /api/v1/auth/registerbio requires JWT validation (protected endpoint)
        // Check this FIRST before other bypass rules to ensure it's protected
        if (path.equals("/api/v1/auth/registerbio") ||
            path.equals("/api/v1/auth/registerbio/") ||
            path.equals("/auth-service/api/v1/auth/registerbio") ||
            path.equals("/auth-service/api/v1/auth/registerbio/") ||
            path.equals("/api/v1/auth/logout") ||
            path.equals("/api/v1/auth/logout/") ||
            path.equals("/auth-service/api/v1/auth/logout") ||
            path.equals("/auth-service/api/v1/auth/logout/")) {
            return false; // Do NOT bypass - require JWT validation
        }

        // Bypass JWT validation for all auth-service endpoints
        // Check for auth-service prefix (when accessed through gateway/load balancer)
        if (path.startsWith("/auth-service")) {
            return true;
        }

        // Check for auth-service endpoints directly (when context-path is stripped by servlet container)
        // Since auth-service has context-path=/auth-service, when running in auth-service,
        // the request URI will be /api/v1/auth/...
        // Most endpoints are unique to auth-service and should bypass JWT (registration/login endpoints)
        if (path.startsWith("/api/v1/auth/")) {
            return true;
        }

        // Bypass JWT validation for encrypt/decrypt endpoints (available in all services)
        // These are utility endpoints for testing encryption/decryption
        if (path.equals("/encrypt") ||
                path.equals("/decrypt")) {
            return true;
        }

        // All other endpoints require JWT validation
        return false;
    }

    /**
     * Response wrapper to capture the response body for encryption
     */
    private static class ResponseWrapper extends HttpServletResponseWrapper {
        private final ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        private final StringWriter writer = new StringWriter();
        private boolean usingWriter = false;
        private boolean usingOutputStream = false;

        public ResponseWrapper(HttpServletResponse response) {
            super(response);
        }

        @Override
        public ServletOutputStream getOutputStream() throws IOException {
            if (usingWriter) {
                throw new IllegalStateException("getWriter() has already been called");
            }
            usingOutputStream = true;
            return new ServletOutputStream() {
                @Override
                public void write(int b) throws IOException {
                    outputStream.write(b);
                }

                @Override
                public void write(byte[] b, int off, int len) throws IOException {
                    outputStream.write(b, off, len);
                }

                @Override
                public boolean isReady() {
                    return true;
                }

                @Override
                public void setWriteListener(jakarta.servlet.WriteListener listener) {
                    // Not needed for synchronous operations
                }
            };
        }

        @Override
        public PrintWriter getWriter() throws IOException {
            if (usingOutputStream) {
                throw new IllegalStateException("getOutputStream() has already been called");
            }
            usingWriter = true;
            return new PrintWriter(writer) {
                @Override
                public void close() {
                    super.close();
                }
            };
        }

        public String getResponseBody() {
            if (usingWriter) {
                return writer.toString();
            } else if (usingOutputStream) {
                return new String(outputStream.toByteArray(), StandardCharsets.UTF_8);
            }
            return null;
        }
    }
}
