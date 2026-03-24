package com.medgo.jwt;

import com.medgo.constant.JWTConstants;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Autowired;

import javax.crypto.SecretKey;
import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

@Component
public class JwtTokenUtil implements Serializable {

    private static final long serialVersionUID = -2550185165626007488L;
    public final Map<String, String> tokenStore = new ConcurrentHashMap<>();
    // Blacklist of invalidated tokens -> expiration timestamp (ms). Tokens in this map are rejected.
    public final Map<String, Long> tokenBlacklist = new ConcurrentHashMap<>();

    private static final Logger log = LoggerFactory.getLogger(JwtTokenUtil.class);
    String SECRET_KEY ="l5p2S2aXzXmBf4dhPbD/xIbb2NZz+Do5rS3T12STIXWKzVGInGdb+jWTLhfgcG5t+bdCD4gq7d0nMZtkIhY+YQ==";
    private final SecretKey secret = Keys.hmacShaKeyFor(SECRET_KEY.getBytes());

    private final Map<String, Object> claims = new HashMap<>();
    
    @Autowired(required = false)
    private TokenBlacklistStore tokenBlacklistStore;


    public String getUsernameFromToken(String token) {
        return getClaimFromToken(token, Claims::getSubject);
    }

    public Date getIssuedAtDateFromToken(String token) {
        return getClaimFromToken(token, Claims::getIssuedAt);
    }

    public Date getExpirationDateFromToken(String token) {
        return getClaimFromToken(token, Claims::getExpiration);
    }

    public <T> T getClaimFromToken(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = getAllClaimsFromToken(token);
        return claimsResolver.apply(claims);
    }

    @SuppressWarnings("deprecation")
    private Claims getAllClaimsFromToken(String token) {
        return Jwts.parser().setSigningKey(secret).parseClaimsJws(token).getBody();
    }

    private Boolean isTokenExpired(String token) {
        final Date expiration = getExpirationDateFromToken(token);
        return expiration.before(new Date());
    }

    private Boolean ignoreTokenExpiration(String token) {
        // here you specify tokens, for that the expiration is ignored
        return false;
    }

    public String generateToken(UserDetails userDetails) {
        return doGenerateToken(userDetails.getUsername());
    }

    /**
     * Generate token with username (email/mobile) claim
     * @param userDetails UserDetails containing deviceId as username
     * @param username User identifier (email or mobile) to include in token
     * @return JWT token with username claim
     */
    public String generateToken(UserDetails userDetails, String username) {
        return doGenerateToken(userDetails.getUsername(), username);
    }

    /**
     * Generate token with username (email/mobile) and memberCode claims
     * @param userDetails UserDetails containing deviceId as username
     * @param username User identifier (email or mobile) to include in token
     * @param memberCode Member code to include in token
     * @return JWT token with username and memberCode claims
     */
    public String generateToken(UserDetails userDetails, String username, String memberCode) {
        return doGenerateToken(userDetails.getUsername(), username, memberCode);
    }

    @SuppressWarnings("deprecation")
    private String doGenerateToken(String subject) {
        String token = Jwts.builder()
                .setSubject(subject)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + JWTConstants.JWT_TOKEN_VALIDITY))
                .signWith(SignatureAlgorithm.HS512, secret)
                .compact();

        tokenStore.put(token, subject); // safe registry
        return token;
    }

    /**
     * Generate token with username claim
     * @param subject DeviceId (used as token subject)
     * @param username Email or mobile (stored as claim)
     * @return JWT token
     */
    @SuppressWarnings("deprecation")
    private String doGenerateToken(String subject, String username) {
        String token = Jwts.builder()
                .setSubject(subject)  // deviceId is the subject
                .claim("username", username) // username (email/mobile) is stored as claim
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + JWTConstants.JWT_TOKEN_VALIDITY))
                .signWith(SignatureAlgorithm.HS512, secret)
                .compact();

        tokenStore.put(token, subject); // safe registry
        return token;
    }

    /**
     * Generate token with username and memberCode claims
     * @param subject DeviceId (used as token subject)
     * @param username Email or mobile (stored as claim)
     * @param memberCode Member code (stored as claim)
     * @return JWT token
     */
    @SuppressWarnings("deprecation")
    private String doGenerateToken(String subject, String username, String memberCode) {
        String token = Jwts.builder()
                .setSubject(subject)  // deviceId is the subject
                .claim("username", username) // username (email/mobile) is stored as claim
                .claim("memberCode", memberCode != null ? memberCode : "") // memberCode is stored as claim
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + JWTConstants.JWT_TOKEN_VALIDITY))
                .signWith(SignatureAlgorithm.HS512, secret)
                .compact();

        tokenStore.put(token, subject); // safe registry
        return token;
    }

    /**
     * Extract username (email/mobile) from token claim
     * @param token JWT token
     * @return username from token claim, or null if not present
     */
    public String getUsernameFromTokenClaim(String token) {
        try {
            Claims claims = getAllClaimsFromToken(token);
            return claims.get("username", String.class);
        } catch (Exception e) {
            log.error("Error extracting username from token: {}", e.getMessage());
            return null;
        }
    }

    /**
     * Extract memberCode from token claim
     * @param token JWT token
     * @return memberCode from token claim, or null if not present
     */
    public String getMemberCodeFromTokenClaim(String token) {
        try {
            Claims claims = getAllClaimsFromToken(token);
            return claims.get("memberCode", String.class);
        } catch (Exception e) {
            log.error("Error extracting memberCode from token: {}", e.getMessage());
            return null;
        }
    }

    public boolean validateToken(String token, UserDetails userDetails) {
        try {
            // Validate token signature and expiration
            // In a distributed system, we don't rely on tokenStore as it's service-specific
            // Instead, we validate the JWT signature (which proves it was signed by auth-service)
            // and check expiration
            String usernameFromToken = getUsernameFromToken(token);
            boolean isExpired = isTokenExpired(token);
            boolean usernameMatches = usernameFromToken.equals(userDetails.getUsername());

            // Token is valid if: signature is valid (parsed successfully), username matches, and not expired
            // Note: If getAllClaimsFromToken throws exception, it means signature is invalid
            boolean isValid = usernameMatches && !isExpired;

            // Optionally check tokenStore if token exists (for extra validation in same service)
            // But don't fail if token is not in store (as it might be from another service)
            String storedUser = tokenStore.get(token);
            if (storedUser != null) {
                // Token is in local store, extra validation
                isValid = isValid && storedUser.equals(userDetails.getUsername());
            }

            // Check local in-memory blacklist first
            if (isTokenBlacklisted(token)) {
                log.warn("Token is blacklisted in local tokenBlacklist");
                return false;
            }

            // Check external blacklist store (Redis) if available
            if (tokenBlacklistStore != null && tokenBlacklistStore.isBlacklisted(token)) {
                log.warn("Token is blacklisted via TokenBlacklistStore");
                return false;
            }

            return isValid;
        } catch (Exception e) {
            // If we can't parse the token, signature is invalid
            log.error("Token validation error: {}", e.getMessage());
            return false;
        }
    }


    public Boolean canTokenBeRefreshed(String token) {
        return (!isTokenExpired(token) || ignoreTokenExpiration(token));
    }

    /**
     * Blacklist a token so it will be rejected by all services that check the blacklist.
     * Also removes it from the local tokenStore.
     *
     * @param token raw JWT token
     */
    public void blacklistToken(String token) {
        try {
            Date exp = getExpirationDateFromToken(token);
            if (exp != null) {
                tokenBlacklist.put(token, exp.getTime());
            } else {
                tokenBlacklist.put(token, System.currentTimeMillis());
            }
        } catch (Exception e) {
            // If parsing fails, still add to blacklist with current time (will be removed by cleanup)
            tokenBlacklist.put(token, System.currentTimeMillis());
        }
        // Remove from tokenStore to avoid accidental whitelist behavior
        tokenStore.remove(token);
    }

    /**
     * Check whether a token is blacklisted. Expired blacklist entries are removed lazily.
     *
     * @param token raw JWT token
     * @return true if token is blacklisted and not yet expired
     */
    public boolean isTokenBlacklisted(String token) {
        Long expTs = tokenBlacklist.get(token);
        if (expTs == null) {
            return false;
        }
        if (expTs < System.currentTimeMillis()) {
            // blacklisted token expiry passed — cleanup
            tokenBlacklist.remove(token);
            return false;
        }
        return true;
    }
}