# JWT Integration Quick Reference Guide

## Overview
This guide provides a quick reference for integrating JWT authentication into microservices.

## Authentication Requirements

### Service Categories

**Shared Services (Basic Auth + API Key - NO CHANGES)**
- shared-membership-service
- shared-provider-service
- shared-reimbursement-service
- shared-utilization-service
- shared-virtualid-service
- shared-notification-service

**User-Facing Services (JWT Only - INTEGRATION REQUIRED)**
- auth-service
- membership-service
- appointment-service
- claims-service
- communication-service
- facescan-service
- filemanagement-service
- loa-service
- reimbursement-service
- provider-service
- provider-notification-service

## Architecture for User-Facing Services (JWT Only)

```
┌─────────────────────────────────────────────────────────────┐
│                    Client Request                            │
│  Headers: Authorization: Bearer <token>, userId: <id>       │
└──────────────────────┬──────────────────────────────────────┘
                       │
                       ▼
┌─────────────────────────────────────────────────────────────┐
│              JwtRequestFilter (common-objects)               │
│  • Validates JWT token                                       │
│  • Checks userId header                                      │
│  • Refreshes token                                           │
│  • Sets SecurityContext                                      │
└──────────────────────┬──────────────────────────────────────┘
                       │
                       ▼
┌─────────────────────────────────────────────────────────────┐
│              SecurityConfig (Service-specific)               │
│  • JWT Filter only                                           │
│  • Bypass URLs                                               │
│  • NO Basic Auth                                             │
│  • NO API Key                                                │
└──────────────────────┬──────────────────────────────────────┘
                       │
                       ▼
┌─────────────────────────────────────────────────────────────┐
│                    Controller                                │
└─────────────────────────────────────────────────────────────┘
```

## Integration Steps for User-Facing Services

### Step 1: Update SecurityConfig (Replace Existing)

**Before (Current - Basic Auth + API Key)**:
```java
@Bean
SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
    http
        .csrf(csrf -> csrf.disable())
        .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
        .httpBasic(Customizer.withDefaults())
        .authorizeHttpRequests(reg -> reg
            .requestMatchers("/health", "/actuator/health").permitAll()
            .anyRequest().authenticated()
        );
    
    if (apiKeyRequired) {
        http.addFilterAfter(apiKeyFilter(), BasicAuthenticationFilter.class);
    }
    
    return http.build();
}
```

**After (JWT Only - Remove Basic Auth and API Key)**:
```java
@Bean
SecurityFilterChain filterChain(HttpSecurity http, 
                                 JwtRequestFilter jwtRequestFilter) throws Exception {
    http
        .csrf(csrf -> csrf.disable())
        .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
        .authorizeHttpRequests(reg -> reg
            .requestMatchers("/health", "/actuator/health").permitAll()
            .anyRequest().authenticated()
        )
        .addFilterBefore(jwtRequestFilter, UsernamePasswordAuthenticationFilter.class);
    
    // NO Basic Auth
    // NO API Key Filter
    
    return http.build();
}
```

### Step 2: Add Application Properties

```properties
# JWT Configuration
jwt.enabled=true
jwt.secret-key=${JWT_SECRET_KEY}
jwt.token-validity=600000

# Service-specific bypass URLs
jwt.security.bypass.urls=/health,/actuator/health,/api/v1/generateToken
```

### Step 3: Configure Azure Key Vault (if using)

```java
@PostConstruct
public void init() {
    // ... existing code ...
    this.jwtSecretKey = keyVaultService.getSecretValue("jwt-secret-key");
}
```

### Step 4: Verify Dependencies

Ensure `build.gradle` includes:
```gradle
implementation "com.medgo:common-objects:0.1.0"
implementation "com.medgo:logging-utils:0.1.0"
```

## Bypass URLs Configuration

### Service-Specific Bypass URLs

| Service | Common Bypass URLs |
|---------|-------------------|
| auth-service | `/api/v1/generateToken`, `/encrypt`, `/decrypt` |
| facescan-service | `/api/v1/faceScan/eligibility`, `/api/v1/faceScan/acceptTnc` |
| All Services | `/health`, `/actuator/health`, `/actuator/**` |

## Shared Services (No Changes Required)

**Shared services continue using Basic Auth + API Key:**
- No JWT integration needed
- Keep existing SecurityConfig
- Keep Basic Auth and API Key filters
- No changes to application properties

## JWT Token Flow

### 1. Token Generation (auth-service)
```
POST /auth-service/api/v1/generateToken
Response: { "token": "<jwt-token>" }
```

### 2. Token Usage
```
GET /service/api/v1/endpoint
Headers:
  Authorization: Bearer <jwt-token>
  userId: <user-id>
```

### 3. Token Refresh
```
Response Header: X-Access-Token: <new-jwt-token>
```

## Error Handling

### Common Error Responses

| Status Code | Error Code | Message |
|------------|------------|---------|
| 401 | 103 | Invalid JWT Token |
| 419 | 104 | Token Expired |
| 403 | 102 | Unauthorized |

### Error Response Format
```json
{
  "statusCode": "103",
  "response": "invalid",
  "data": {
    "encryptedData": "...",
    "encryptedKey": "...",
    "iv": "..."
  },
  "traceId": "<trace-id>"
}
```

## Testing

### Test with Valid Token
```bash
curl -X GET http://localhost:8080/api/v1/endpoint \
  -H "Authorization: Bearer <valid-token>" \
  -H "userId: <user-id>"
```

### Test with Invalid Token
```bash
curl -X GET http://localhost:8080/api/v1/endpoint \
  -H "Authorization: Bearer invalid-token" \
  -H "userId: <user-id>"
```

### Test Bypass URL
```bash
curl -X GET http://localhost:8080/health
# Should work without JWT token
```

## Troubleshooting

### Issue: JWT Filter not being invoked
- **Solution**: Check filter order in SecurityConfig
- **Solution**: Verify JwtRequestFilter bean is created

### Issue: Token validation fails
- **Solution**: Check JWT secret key matches token issuer
- **Solution**: Verify token is not expired
- **Solution**: Check userId header matches token subject

### Issue: Bypass URLs not working
- **Solution**: Verify URL pattern matches exactly
- **Solution**: Check configuration properties
- **Solution**: Review filter order

### Issue: Encryption errors
- **Solution**: Verify EncryptionService bean is available
- **Solution**: Check encryption configuration
- **Solution**: Review error logs

## Migration Checklist

### Pre-Integration
- [ ] Review JWT Integration Plan
- [ ] Verify service category (shared vs user-facing)
- [ ] Verify common-objects version
- [ ] Prepare test environment
- [ ] Generate test JWT tokens

### Integration (User-Facing Services Only)
- [ ] Remove Basic Auth from SecurityConfig
- [ ] Remove API Key Filter from SecurityConfig
- [ ] Add JWT Filter to SecurityConfig
- [ ] Add application properties
- [ ] Configure bypass URLs
- [ ] Update Azure Key Vault (if needed)
- [ ] Test JWT validation
- [ ] Test bypass URLs
- [ ] Test error handling
- [ ] Verify Basic Auth is disabled
- [ ] Verify API Key is disabled

### Shared Services (No Changes)
- [x] Verify shared services use Basic Auth + API Key
- [x] No changes needed

### Post-Integration
- [ ] Update API documentation
- [ ] Update service README
- [ ] Perform integration testing
- [ ] Deploy to staging
- [ ] Monitor logs
- [ ] Deploy to production

## Key Files to Modify

### Common Objects
- `shared/common-objects/src/main/java/com/medgo/filter/JwtRequestFilter.java`
- `shared/common-objects/src/main/java/com/medgo/config/JwtWebSecurityConfig.java`

### Microservice
- `services/<service-name>/src/main/java/<package>/config/SecurityConfig.java`
- `services/<service-name>/src/main/resources/application.properties`

## Support and Resources

- **JWT Integration Plan**: `docs/JWT_Integration_Plan.md`
- **Common Objects**: `shared/common-objects/`
- **Example SecurityConfig**: `services/shared-provider-service/src/main/java/com/medgo/provider/config/SecurityConfig.java`

---

**Last Updated**: 2024  
**Version**: 1.0

