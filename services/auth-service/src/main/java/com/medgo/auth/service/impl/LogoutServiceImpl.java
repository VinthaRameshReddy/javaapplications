package com.medgo.auth.service.impl;

import com.medgo.auth.service.LogoutService;
import com.medgo.commons.CommonResponse;
import com.medgo.commons.ErrorResponse;
import com.medgo.jwt.JwtTokenUtil;
import com.medgo.jwt.TokenBlacklistStore;
import org.springframework.beans.factory.annotation.Autowired;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class LogoutServiceImpl implements LogoutService {
    private final Logger logger = LoggerFactory.getLogger(LogoutServiceImpl.class);
    private final JwtTokenUtil jwtTokenUtil;
    
    @Autowired(required = false)
    private TokenBlacklistStore tokenBlacklistStore;

    @Override
    public CommonResponse logout(String token, String userId) {
        if (token == null || token.isBlank()) {
            return CommonResponse.error(new ErrorResponse(401, "INVALID_JWT", "Authorization token missing"), 401);
        }

        try {
            // The token subject is the deviceId; username/email is stored as a claim.
            // Validate that the token's username claim matches the provided userId (email).
            String tokenUsername = jwtTokenUtil.getUsernameFromTokenClaim(token);
            if (tokenUsername == null || !tokenUsername.equals(userId)) {
                logger.warn("Token username claim '{}' does not match requested userId '{}'", tokenUsername, userId);
                return CommonResponse.error(new ErrorResponse(401, "INVALID_TOKEN_USER", "Token does not belong to provided user"), 401);
            }

            // Compute remaining TTL for token and blacklist in Redis (if available)
            try {
                java.util.Date exp = jwtTokenUtil.getExpirationDateFromToken(token);
                long ttlSeconds = 60; // default minimal TTL
                if (exp != null) {
                    long now = System.currentTimeMillis();
                    ttlSeconds = Math.max(1, (exp.getTime() - now) / 1000);
                }

                if (tokenBlacklistStore != null) {
                    tokenBlacklistStore.blacklist(token, ttlSeconds);
                    logger.info("Token blacklisted in external store for {} seconds", ttlSeconds);
                }
            } catch (Exception e) {
                logger.warn("Could not compute TTL or blacklist in external store: {}", e.getMessage());
            }

            // Also mark locally so auth-service checks reject it
            jwtTokenUtil.blacklistToken(token);
            logger.info("Token blacklisted/invalidated for userId={}", userId);
            return CommonResponse.success("User logged out successfully");
        } catch (Exception e) {
            logger.error("Error while invalidating token: {}", e.getMessage(), e);
            return CommonResponse.error(new ErrorResponse(500, "LOGOUT_FAILED", "Failed to logout"), 500);
        }
    }
}

