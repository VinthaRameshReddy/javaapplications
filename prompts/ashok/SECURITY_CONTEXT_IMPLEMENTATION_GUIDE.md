# Security Context Implementation Guide for Auth Service

## Overview
This guide explains how to add Spring Security with SecurityContext to automatically validate JWT tokens and make authenticated user information available throughout the application without manual token extraction.

---

## Current State vs. Proposed State

### Current Implementation (Manual Token Extraction)
```
Controller → Service → Manual Token Extraction → Manual Validation → Business Logic
```

### Proposed Implementation (Security Context)
```
Controller → Security Filter → Automatic Token Validation → SecurityContext → Business Logic
```

---

## Architecture Flow Diagram

```
┌─────────────────────────────────────────────────────────────────────────┐
│                         CLIENT REQUEST                                   │
│              POST /api/v1/auth/userDetails                               │
│              Headers:                                                    │
│              - Authorization: Bearer <JWT_TOKEN>                         │
│              - X-Access-Token: <JWT_TOKEN> (alternative)                │
│              - userId: <deviceId>                                        │
└──────────────────────────────┬──────────────────────────────────────────┘
                                │
                                ▼
┌─────────────────────────────────────────────────────────────────────────┐
│                    SPRING SECURITY FILTER CHAIN                          │
│  SecurityFilterChain Configuration                                        │
│  • Disables default login form                                           │
│  • Configures JWT authentication filter                                   │
│  • Sets up security context                                              │
└──────────────────────────────┬──────────────────────────────────────────┘
                                │
                                ▼
┌─────────────────────────────────────────────────────────────────────────┐
│                    JWT AUTHENTICATION FILTER                             │
│  JwtAuthenticationFilter extends OncePerRequestFilter                    │
│                                                                           │
│  1. Extract Token:                                                        │
│     - Check Authorization header: "Bearer <token>"                      │
│     - Fallback: X-Access-Token header                                    │
│     - Return null if not found                                           │
│                                                                           │
│  2. Validate Token:                                                      │
│     - Extract deviceId from token (subject)                              │
│     - Load UserDetails using deviceId                                    │
│     - Validate token signature and expiration                            │
│     - Extract username from token claims                                │
│                                                                           │
│  3. Create Authentication:                                                │
│     - Create UsernamePasswordAuthenticationToken                         │
│     - Set principal: UserDetails                                         │
│     - Set authorities: User roles/permissions                            │
│     - Set authenticated: true                                            │
│                                                                           │
│  4. Set Security Context:                                                 │
│     - SecurityContextHolder.getContext().setAuthentication(auth)         │
│     - Makes authentication available to entire request                   │
└──────────────────────────────┬──────────────────────────────────────────┘
                                │
                                ▼
┌─────────────────────────────────────────────────────────────────────────┐
│                    CONTROLLER LAYER                                      │
│  @RestController                                                          │
│  • Can access SecurityContext directly                                   │
│  • No manual token extraction needed                                     │
│  • Use @AuthenticationPrincipal for user details                        │
└──────────────────────────────┬──────────────────────────────────────────┘
                                │
                                ▼
┌─────────────────────────────────────────────────────────────────────────┐
│                    SERVICE LAYER                                         │
│  @Service                                                                 │
│  • Access SecurityContext: SecurityContextHolder.getContext()           │
│  • Get Authentication: context.getAuthentication()                     │
│  • Extract user info: authentication.getPrincipal()                      │
│  • No manual token extraction needed                                     │
└─────────────────────────────────────────────────────────────────────────┘
```

---

## Implementation Steps

### Step 1: Add Spring Security Dependencies

**File: `build.gradle`**

```gradle
dependencies {
    // Existing dependencies...
    
    // Spring Security
    implementation 'org.springframework.boot:spring-boot-starter-security'
    
    // JWT (if not already included)
    // implementation 'io.jsonwebtoken:jjwt-api:0.11.5'
    // implementation 'io.jsonwebtoken:jjwt-impl:0.11.5'
    // implementation 'io.jsonwebtoken:jjwt-jackson:0.11.5'
}
```

---

### Step 2: Create JWT Authentication Filter

**File: `src/main/java/com/medgo/auth/security/JwtAuthenticationFilter.java`**

```java
package com.medgo.auth.security;

import com.medgo.jwt.JwtTokenUtil;
import com.medgo.jwt.JwtUserDetailsService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenUtil jwtTokenUtil;
    private final JwtUserDetailsService jwtUserDetailsService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, 
                                    HttpServletResponse response, 
                                    FilterChain filterChain) 
            throws ServletException, IOException {
        
        try {
            // Step 1: Extract JWT token from request
            String token = extractTokenFromRequest(request);
            
            if (token != null && !token.isEmpty()) {
                // Step 2: Extract deviceId from token (token subject)
                String deviceId = jwtTokenUtil.getUsernameFromToken(token);
                
                if (deviceId != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                    // Step 3: Load user details using deviceId
                    UserDetails userDetails = jwtUserDetailsService.loadUserByUsername(deviceId);
                    
                    // Step 4: Validate token
                    if (jwtTokenUtil.validateToken(token, userDetails)) {
                        // Step 5: Create authentication object
                        UsernamePasswordAuthenticationToken authentication = 
                            new UsernamePasswordAuthenticationToken(
                                userDetails,
                                null,
                                userDetails.getAuthorities()
                            );
                        
                        // Step 6: Set request details
                        authentication.setDetails(
                            new WebAuthenticationDetailsSource().buildDetails(request)
                        );
                        
                        // Step 7: Set authentication in SecurityContext
                        SecurityContextHolder.getContext().setAuthentication(authentication);
                        
                        log.debug("JWT token validated and SecurityContext set for deviceId: {}", deviceId);
                    } else {
                        log.warn("Invalid JWT token for deviceId: {}", deviceId);
                    }
                }
            }
        } catch (Exception e) {
            log.error("Error setting authentication in SecurityContext: {}", e.getMessage(), e);
            // Continue filter chain - let controller handle authentication errors
        }
        
        // Continue filter chain
        filterChain.doFilter(request, response);
    }

    /**
     * Extract JWT token from request headers
     * Priority: Authorization header (Bearer token) > X-Access-Token header
     */
    private String extractTokenFromRequest(HttpServletRequest request) {
        // Try Authorization header first
        String bearerToken = request.getHeader("Authorization");
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        
        // Fallback to X-Access-Token header
        String accessToken = request.getHeader("X-Access-Token");
        if (accessToken != null && !accessToken.isEmpty()) {
            return accessToken;
        }
        
        return null;
    }

    /**
     * Configure which paths should skip JWT authentication
     * Public endpoints like login, registration, OTP don't need JWT
     */
    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        
        // Skip JWT filter for public endpoints
        return path.startsWith("/api/v1/auth/login") ||
               path.startsWith("/api/v1/auth/member/requestOtp") ||
               path.startsWith("/api/v1/auth/nonmember/requestOtp") ||
               path.startsWith("/api/v1/auth/verifyOtp") ||
               path.startsWith("/api/v1/auth/resendOtp") ||
               path.startsWith("/api/v1/auth/register") ||
               path.startsWith("/api/v1/auth/validateMemberRegDetails") ||
               path.startsWith("/api/v1/auth/validateNonMemberDetails") ||
               path.startsWith("/api/v1/auth/setPassword") ||
               path.startsWith("/api/v1/auth/resetPassword") ||
               path.startsWith("/api/v1/auth/biometric/challenge") ||
               path.startsWith("/api/v1/auth/biometric/storePasskeyHash") ||
               path.startsWith("/api/v1/generateToken") ||
               path.startsWith("/actuator") ||
               path.startsWith("/encrypt") ||
               path.startsWith("/decrypt");
    }
}
```

---

### Step 3: Create Security Configuration

**File: `src/main/java/com/medgo/auth/config/SecurityConfig.java`**

```java
package com.medgo.auth.config;

import com.medgo.auth.security.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            // Disable CSRF (not needed for stateless JWT authentication)
            .csrf(csrf -> csrf.disable())
            
            // Configure CORS
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            
            // Stateless session management (JWT tokens, no server-side sessions)
            .sessionManagement(session -> 
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            
            // Configure authorization rules
            .authorizeHttpRequests(auth -> auth
                // Public endpoints (no authentication required)
                .requestMatchers(
                    "/api/v1/auth/login/**",
                    "/api/v1/auth/member/requestOtp",
                    "/api/v1/auth/nonmember/requestOtp",
                    "/api/v1/auth/verifyOtp",
                    "/api/v1/auth/resendOtp",
                    "/api/v1/auth/register/**",
                    "/api/v1/auth/validateMemberRegDetails",
                    "/api/v1/auth/validateNonMemberDetails",
                    "/api/v1/auth/setPasswordMember",
                    "/api/v1/auth/setPasswordNonMember",
                    "/api/v1/auth/member/resetPassword",
                    "/api/v1/auth/nonmember/resetPassword",
                    "/api/v1/auth/biometric/challenge",
                    "/api/v1/auth/biometric/storePasskeyHash",
                    "/api/v1/generateToken",
                    "/actuator/**",
                    "/encrypt",
                    "/decrypt"
                ).permitAll()
                
                // All other endpoints require authentication
                .anyRequest().authenticated()
            )
            
            // Add JWT authentication filter before UsernamePasswordAuthenticationFilter
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
        
        return http.build();
    }

    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(List.of("*")); // Configure appropriately for production
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setExposedHeaders(List.of("X-Access-Token"));
        configuration.setAllowCredentials(false);
        
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
```

---

### Step 4: Update Service Layer to Use SecurityContext

**File: `src/main/java/com/medgo/auth/serviceImpl/AuthServiceImpl.java`**

```java
// Add import
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;

// Update getUserDetails method
public CommonResponse getUserDetails(UserDetailsRequest request) {
    LOGGER.info("GetUserDetailsRequest: {}", request.userId());

    // Step 1: Get authentication from SecurityContext (set by JWT filter)
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    
    if (authentication == null || !authentication.isAuthenticated()) {
        LOGGER.warn("No authentication found in SecurityContext");
        return CommonResponse.error(
            new ErrorResponse(HttpStatus.UNAUTHORIZED.value(),
                    "Authentication required. Please provide valid JWT token."),
            HttpStatus.UNAUTHORIZED.value()
        );
    }

    // Step 2: Extract UserDetails from authentication
    UserDetails userDetails = (UserDetails) authentication.getPrincipal();
    String deviceId = userDetails.getUsername(); // deviceId is the username in UserDetails
    
    // Step 3: Validate userId header matches deviceId
    String headerUserId = getUserIdFromRequest();
    if (headerUserId == null || headerUserId.isEmpty()) {
        LOGGER.error("userId header is missing from request");
        return CommonResponse.error(
            new ErrorResponse(HttpStatus.UNAUTHORIZED.value(),
                    "Missing mandatory header: userId"),
            HttpStatus.UNAUTHORIZED.value()
        );
    }
    
    if (!headerUserId.equals(deviceId)) {
        LOGGER.warn("userId header mismatch: headerUserId={}, deviceId={}", headerUserId, deviceId);
        return CommonResponse.error(
            new ErrorResponse(HttpStatus.UNAUTHORIZED.value(),
                    "userId header does not match authenticated deviceId"),
            HttpStatus.UNAUTHORIZED.value()
        );
    }

    // Step 4: Extract username from token claims (if needed)
    // Note: You may need to store username in UserDetails or extract from token
    String tokenUsername = extractUsernameFromAuthentication(authentication);
    
    if (tokenUsername == null || tokenUsername.isEmpty()) {
        LOGGER.warn("Username not found in authentication");
        return CommonResponse.error(
            new ErrorResponse(HttpStatus.UNAUTHORIZED.value(),
                    "Invalid authentication: username not found"),
            HttpStatus.UNAUTHORIZED.value()
        );
    }

    // Step 5: Validate request userId matches token username
    if (!tokenUsername.equalsIgnoreCase(request.userId())) {
        LOGGER.warn("Token username mismatch: tokenUsername={}, requestUserId={}", 
                tokenUsername, request.userId());
        return CommonResponse.error(
            new ErrorResponse(HttpStatus.FORBIDDEN.value(),
                    "Token does not belong to the requested user"),
            HttpStatus.FORBIDDEN.value()
        );
    }

    // Step 6: Continue with business logic...
    // (rest of the method remains the same)
}

// Helper method to extract username from authentication
private String extractUsernameFromAuthentication(Authentication authentication) {
    // Option 1: If username is stored in UserDetails
    if (authentication.getPrincipal() instanceof UserDetails) {
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        // You may need to extend UserDetails to include username
        // For now, extract from token if needed
    }
    
    // Option 2: Extract from token claims stored in authentication details
    // You can store custom claims in authentication details during filter
    if (authentication.getDetails() instanceof Map) {
        @SuppressWarnings("unchecked")
        Map<String, Object> details = (Map<String, Object>) authentication.getDetails();
        return (String) details.get("username");
    }
    
    // Option 3: Extract from request header (fallback)
    return getUserIdFromRequest();
}
```

---

### Step 5: Enhanced JWT Filter with Username Storage

**Update: `JwtAuthenticationFilter.java`**

```java
// In doFilterInternal method, after token validation:

if (jwtTokenUtil.validateToken(token, userDetails)) {
    // Extract username from token claims
    String username = jwtTokenUtil.getUsernameFromTokenClaim(token);
    
    // Create authentication object
    UsernamePasswordAuthenticationToken authentication = 
        new UsernamePasswordAuthenticationToken(
            userDetails,
            null,
            userDetails.getAuthorities()
        );
    
    // Store username in authentication details for later retrieval
    Map<String, Object> details = new HashMap<>();
    details.put("username", username);
    details.put("deviceId", deviceId);
    details.put("token", token); // Optional: store token if needed
    authentication.setDetails(details);
    
    // Set authentication in SecurityContext
    SecurityContextHolder.getContext().setAuthentication(authentication);
    
    log.debug("JWT token validated. deviceId: {}, username: {}", deviceId, username);
}
```

---

### Step 6: Controller Usage with @AuthenticationPrincipal

**File: `src/main/java/com/medgo/auth/controller/AuthController.java`**

```java
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;

@PostMapping("/userDetails")
@EncryptResponse
public ResponseEntity<CommonResponse> getUserDetails(
        @Valid @DecryptBody(UserDetailsRequest.class) UserDetailsRequest request,
        @AuthenticationPrincipal UserDetails userDetails) { // Inject authenticated user
    
    logger.info("UserDetailsRequest: userId={}, deviceId={}", 
            request.userId(), userDetails.getUsername());
    
    // Validate deviceId header
    ResponseEntity<CommonResponse> validationError = SecurityValidationUtil.validateDeviceId();
    if (validationError != null) {
        return validationError;
    }
    
    // Service can now use SecurityContext directly
    CommonResponse response = registrationService.getUserDetails(request);
    logger.info("UserDetailsResponse: {}", response);
    return ResponseHeaderUtil.processTokenResponse(response);
}
```

---

## Benefits of Security Context Implementation

### ✅ **Centralized Authentication**
- Single point of JWT validation (filter)
- Consistent authentication across all endpoints
- No duplicate token extraction code

### ✅ **Cleaner Code**
- Controllers don't need manual token extraction
- Services can access authentication directly
- Less boilerplate code

### ✅ **Better Security**
- Automatic token validation for all protected endpoints
- SecurityContext is thread-local (safe for concurrent requests)
- Easy to add role-based access control

### ✅ **Easier Testing**
- Mock SecurityContext in tests
- No need to mock request headers
- Test authentication logic separately

### ✅ **Spring Security Integration**
- Leverage Spring Security features (method security, etc.)
- Standard Spring Security patterns
- Better integration with other Spring components

---

## Migration Strategy

### Phase 1: Add Security Context (Non-Breaking)
1. Add Spring Security dependencies
2. Create JWT filter and security config
3. Configure public endpoints (no authentication required)
4. Test that existing endpoints still work

### Phase 2: Update Service Methods (Gradual)
1. Update one service method at a time
2. Use SecurityContext instead of manual token extraction
3. Test each method thoroughly
4. Keep old methods as fallback initially

### Phase 3: Cleanup
1. Remove manual token extraction methods
2. Remove duplicate validation code
3. Update all controllers to use SecurityContext
4. Update documentation

---

## Testing Security Context

### Unit Test Example

```java
@Test
void testGetUserDetails_WithSecurityContext() {
    // Setup SecurityContext
    UserDetails userDetails = mock(UserDetails.class);
    when(userDetails.getUsername()).thenReturn("device-123");
    
    Authentication authentication = new UsernamePasswordAuthenticationToken(
        userDetails, null, userDetails.getAuthorities());
    SecurityContextHolder.getContext().setAuthentication(authentication);
    
    // Test service method
    UserDetailsRequest request = new UserDetailsRequest("user@example.com");
    CommonResponse response = authService.getUserDetails(request);
    
    // Verify
    assertTrue(response.isSuccess());
    verify(userRepository).findByEmailOrMobile(anyString(), anyString());
    
    // Cleanup
    SecurityContextHolder.clearContext();
}
```

---

## Configuration Properties

**File: `application.properties`**

```properties
# JWT Configuration
jwt.secret=your-secret-key-here
jwt.expiration=86400000  # 24 hours in milliseconds

# Security Configuration
security.public-endpoints=/api/v1/auth/login/**,/api/v1/auth/register/**
```

---

## Troubleshooting

### Issue: SecurityContext is null
**Solution**: Ensure JWT filter is properly configured and token is valid

### Issue: Authentication fails for public endpoints
**Solution**: Check `shouldNotFilter()` method in JWT filter and security config

### Issue: CORS errors
**Solution**: Configure CORS properly in `SecurityConfig.corsConfigurationSource()`

### Issue: Token validation fails
**Solution**: Verify JWT secret key matches between token generation and validation

---

## Summary

Adding SecurityContext to the auth service provides:
- **Automatic JWT validation** via filter
- **Centralized authentication** logic
- **Cleaner code** with less boilerplate
- **Better security** with Spring Security integration
- **Easier testing** with mockable SecurityContext

The implementation follows Spring Security best practices and makes the codebase more maintainable and secure.




