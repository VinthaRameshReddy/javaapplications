# JWT Filter Integration Plan

## Executive Summary
This document outlines the plan to integrate JWT (JSON Web Token) authentication filter logic from `common-objects` and `logging-utils` shared modules into existing microservices. The JWT filter is currently present but disabled in the codebase.

## Current State Analysis

### 1. JWT Components in `common-objects`
- **JwtRequestFilter** (`com.medgo.filter.JwtRequestFilter`) - Currently disabled (logic commented out)
- **JwtWebSecurityConfig** (`com.medgo.config.JwtWebSecurityConfig`) - Security disabled
- **JwtTokenUtil** (`com.medgo.jwt.JwtTokenUtil`) - Active JWT utility class
- **JwtUserDetailsService** (`com.medgo.jwt.JwtUserDetailsService`) - Active user details service
- **JWTConstants** (`com.medgo.constant.JWTConstants`) - JWT configuration constants

### 2. Current Microservice Security
- Most services use **API Key + Basic Auth** authentication
- Services have `SecurityConfig` classes with Azure Key Vault integration
- Services already depend on `common-objects:0.1.0` module
- Current security pattern:
  - Basic Authentication (HTTP Basic)
  - API Key validation filter
  - Health endpoints are publicly accessible

### 3. Issues Identified
- JWT filter logic is completely commented out
- Missing imports in `JwtRequestFilter` (StringUtils, SecurityContextHolder, ExpiredJwtException, SignatureException)
- Hardcoded bypass URI list (not configurable)
- No integration with existing SecurityConfig in services
- Encryption service dependency needs to be handled properly

## Integration Strategy

### Authentication Requirements

**Clear Separation of Authentication Methods**:

1. **Shared Services**: Keep **Basic Auth + API Key** (NO JWT)
   - These are service-to-service/internal services
   - Use Basic Authentication + API Key validation
   - No changes needed to security configuration

2. **Other Services**: Use **JWT Only** (NO Basic Auth, NO API Key)
   - These are user-facing services
   - Replace existing Basic Auth + API Key with JWT
   - Simpler security model for user authentication

### Phase 1: Fix and Enable JWT Components in `common-objects`

#### 1.1 Fix JwtRequestFilter
**File**: `shared/common-objects/src/main/java/com/medgo/filter/JwtRequestFilter.java`

**Tasks**:
- Uncomment and fix the JWT validation logic
- Add missing imports:
  - `org.springframework.util.StringUtils`
  - `org.springframework.security.core.context.SecurityContextHolder`
  - `org.springframework.security.authentication.UsernamePasswordAuthenticationToken`
  - `org.springframework.security.web.authentication.WebAuthenticationDetailsSource`
  - `io.jsonwebtoken.ExpiredJwtException`
  - `io.jsonwebtoken.security.SignatureException`
- Make bypass URI list configurable (via application properties or environment variables)
- Fix error handling and response formatting
- Ensure proper integration with EncryptionService

**Key Changes**:
- Enable JWT token validation from `Authorization: Bearer <token>` header
- Validate `userId` header matches token subject
- Generate and return refreshed token in `X-Access-Token` header
- Set SecurityContext with user authentication
- Handle token expiration and signature validation errors

#### 1.2 Update JwtWebSecurityConfig
**File**: `shared/common-objects/src/main/java/com/medgo/config/JwtWebSecurityConfig.java`

**Tasks**:
- Uncomment security configuration
- Configure JWT filter in the security filter chain
- Set up proper CORS headers
- Configure exception handling for authentication failures
- Make bypass endpoints configurable
- **Important**: This config should NOT be auto-configured - services will import it explicitly

#### 1.3 Make Bypass URLs Configurable
**Approach**: Create a configuration properties class or use Spring's `@Value` annotation

**Options**:
1. **Application Properties**: Add `jwt.security.bypass.urls` in application.properties
2. **Environment Variables**: Use environment-specific configuration
3. **Service-specific Configuration**: Allow each service to define its own bypass URLs

**Recommended**: Use application properties with sensible defaults

### Phase 2: Service Categorization and Integration Pattern

#### 2.1 Service Categories

**Category A: Shared Services (Keep Basic Auth + API Key)**
- ✅ **shared-membership-service** - Keep existing Basic Auth + API Key
- ✅ **shared-provider-service** - Keep existing Basic Auth + API Key
- ✅ **shared-reimbursement-service** - Keep existing Basic Auth + API Key
- ✅ **shared-utilization-service** - Keep existing Basic Auth + API Key
- ✅ **shared-virtualid-service** - Keep existing Basic Auth + API Key
- ✅ **shared-notification-service** - Keep existing Basic Auth + API Key

**Category B: User-Facing Services (Use JWT Only)**
- 🔄 **auth-service** - Replace with JWT (token generation service - special handling)
- 🔄 **membership-service** - Replace with JWT
- 🔄 **appointment-service** - Replace with JWT
- 🔄 **claims-service** - Replace with JWT
- 🔄 **communication-service** - Replace with JWT
- 🔄 **facescan-service** - Replace with JWT
- 🔄 **filemanagement-service** - Replace with JWT
- 🔄 **loa-service** - Replace with JWT
- 🔄 **reimbursement-service** - Replace with JWT
- 🔄 **provider-service** - Replace with JWT
- 🔄 **provider-notification-service** - Replace with JWT

#### 2.2 Integration Pattern for User-Facing Services (JWT Only)

**Security Configuration Pattern**:
- Remove Basic Authentication
- Remove API Key Filter
- Use JWT Filter only
- Configure bypass URLs for public endpoints

**Filter Order**:
1. JWT Filter (for user authentication)
2. Application filters

**Security Chain**:
```
Request → JWT Filter → Controller
```

#### 2.3 Integration Pattern for Shared Services (Basic Auth + API Key)

**Security Configuration Pattern**:
- Keep existing Basic Authentication
- Keep existing API Key Filter
- NO JWT Filter
- No changes needed

**Security Chain**:
```
Request → Basic Auth → API Key Filter → Controller
```

### Phase 3: Service-by-Service Integration

#### 3.1 Shared Services (NO CHANGES NEEDED)
These services will continue using Basic Auth + API Key:
1. ✅ **shared-membership-service** - No changes
2. ✅ **shared-provider-service** - No changes
3. ✅ **shared-reimbursement-service** - No changes
4. ✅ **shared-utilization-service** - No changes
5. ✅ **shared-virtualid-service** - No changes
6. ✅ **shared-notification-service** - No changes

#### 3.2 User-Facing Services (JWT INTEGRATION REQUIRED)
These services need JWT integration:
1. 🔄 **auth-service** - Token generation service (special handling for token endpoint)
2. 🔄 **membership-service** - User-facing service
3. 🔄 **appointment-service** - User-facing service
4. 🔄 **claims-service** - User-facing service
5. 🔄 **communication-service** - User-facing service
6. 🔄 **facescan-service** - User-facing service
7. 🔄 **filemanagement-service** - User-facing service
8. 🔄 **loa-service** - User-facing service
9. 🔄 **reimbursement-service** - User-facing service
10. 🔄 **provider-service** - User-facing service
11. 🔄 **provider-notification-service** - User-facing service

#### 3.3 Integration Steps for User-Facing Services (JWT Only)

**Step 1: Verify Dependencies**
- Ensure `common-objects:0.1.0` is in `build.gradle`
- Verify Spring Security dependencies are present
- Check for JWT library dependencies (should be in common-objects)

**Step 2: Update SecurityConfig (Replace Existing)**
- Remove Basic Authentication configuration
- Remove API Key Filter
- Import `JwtRequestFilter` from common-objects
- Add JWT filter to security filter chain
- Configure bypass URLs for service-specific endpoints
- Remove Azure Key Vault dependencies for API Key/Basic Auth (if not needed)

**Step 3: Configure Application Properties**
- Add JWT configuration properties
- Configure bypass URLs (health, actuator, public endpoints)
- Set JWT secret key (from Azure Key Vault or environment)
- Configure token validity duration
- Remove API key and Basic Auth properties (if not needed)

**Step 4: Test Integration**
- Test JWT validation with valid tokens
- Test error handling with invalid/expired tokens
- Test bypass URLs work correctly
- Verify Basic Auth and API Key are disabled
- Test token refresh mechanism

#### 3.4 Special Handling for auth-service
- Token generation endpoint (`/api/v1/generateToken`) must be in bypass URLs
- This service generates tokens, so it should not require JWT for token generation
- Other endpoints may require different authentication (or remain public)

### Phase 4: Configuration Management

#### 4.1 Application Properties Template
```properties
# JWT Configuration
jwt.enabled=true
jwt.secret-key=${JWT_SECRET_KEY}  # From Azure Key Vault
jwt.token-validity=600000  # 10 minutes in milliseconds

# JWT Bypass URLs (service-specific)
jwt.security.bypass.urls=/health,/actuator/health,/api/v1/generateToken

# Encryption (if required)
encryption.enabled=true
```

#### 4.2 Azure Key Vault Integration
- Store JWT secret key in Azure Key Vault
- Update Key Vault service to retrieve JWT secret
- Use same pattern as existing API key retrieval

#### 4.3 Environment-Specific Configuration
- Development: JWT may be optional or use test keys
- Staging: Full JWT validation with staging keys
- Production: Full JWT validation with production keys

### Phase 5: Error Handling and Responses

#### 5.1 JWT Error Responses
- **401 Unauthorized**: Invalid or missing JWT token
- **419 Token Expired**: Expired JWT token
- **403 Forbidden**: Valid token but insufficient permissions

#### 5.2 Response Format
- Use `CommonResponse.error()` for error responses
- Encrypt error responses if encryption is enabled
- Include trace ID in responses
- Follow existing error code patterns (CustomStatusCode)

### Phase 6: Testing Strategy

#### 6.1 Unit Tests
- Test JWT token validation
- Test token expiration handling
- Test bypass URL logic
- Test error response formatting

#### 6.2 Integration Tests
- Test JWT filter in security chain
- Test with valid/invalid tokens
- Test with expired tokens
- Test bypass endpoints

#### 6.3 End-to-End Tests
- Test complete authentication flow
- Test token refresh mechanism
- Test service-to-service communication
- Test user-facing endpoints

## Implementation Checklist

### Common Objects Module
- [ ] Fix JwtRequestFilter imports and logic
- [ ] Make bypass URLs configurable
- [ ] Update JwtWebSecurityConfig
- [ ] Add configuration properties class
- [ ] Test JWT filter independently
- [ ] Update documentation

### Microservice Integration

#### Shared Services (No Changes)
- [x] Verify shared services are using Basic Auth + API Key
- [x] No changes needed for shared services
- [ ] Document that shared services use Basic Auth + API Key

#### User-Facing Services (JWT Integration)
- [ ] Update SecurityConfig in each user-facing service
- [ ] Remove Basic Auth and API Key from user-facing services
- [ ] Add JWT configuration properties
- [ ] Configure bypass URLs per service
- [ ] Add JWT secret to Azure Key Vault
- [ ] Update Key Vault service to retrieve JWT secret (if needed)
- [ ] Test JWT validation in each service
- [ ] Verify Basic Auth and API Key are disabled
- [ ] Update API documentation

### Documentation
- [ ] Update service README files
- [ ] Document JWT configuration
- [ ] Document bypass URL configuration
- [ ] Create migration guide
- [ ] Update API documentation

## Migration Path

### Phase 1: Preparation (Week 1)
1. Fix JWT components in common-objects
2. Make configurations flexible
3. Test JWT filter in isolation
4. Update common-objects version

### Phase 2: Pilot Integration (Week 2)
1. Integrate JWT in one user-facing service (e.g., membership-service)
2. Test thoroughly (JWT validation, error handling, bypass URLs)
3. Verify Basic Auth and API Key are disabled
4. Fix any issues
5. Document findings

### Phase 3: Gradual Rollout (Weeks 3-4)
1. Integrate JWT in remaining user-facing services
2. Monitor and fix issues
3. Verify shared services remain unchanged (Basic Auth + API Key)
4. Complete testing for all services
5. Update documentation

### Phase 4: Production Deployment (Week 5)
1. Deploy to staging
2. Perform end-to-end testing
3. Deploy to production
4. Monitor and optimize

## Risk Mitigation

### Risks
1. **Breaking Changes**: Existing API clients may not have JWT tokens
   - **Mitigation**: Use feature flags, gradual rollout, maintain API key auth initially

2. **Token Management**: Token storage and validation issues
   - **Mitigation**: Use Redis for token storage, implement proper token refresh

3. **Performance**: JWT validation overhead
   - **Mitigation**: Optimize filter logic, use caching where appropriate

4. **Configuration Errors**: Misconfigured bypass URLs
   - **Mitigation**: Comprehensive testing, clear documentation, validation

5. **Secret Key Management**: JWT secret key exposure
   - **Mitigation**: Use Azure Key Vault, environment variables, never commit secrets

## Success Criteria

1. ✅ JWT filter is enabled and working in common-objects
2. ✅ All microservices can use JWT authentication
3. ✅ Bypass URLs are configurable per service
4. ✅ Error handling is consistent across services
5. ✅ Documentation is complete and accurate
6. ✅ All tests pass
7. ✅ Production deployment is successful
8. ✅ No breaking changes for existing clients (during transition)

## Next Steps

1. Review and approve this plan
2. Assign tasks to team members
3. Start Phase 1 implementation
4. Schedule regular check-ins
5. Prepare testing environment
6. Set up monitoring and logging

## Service Authentication Summary

### Shared Services (Basic Auth + API Key)
| Service | Authentication Method | Status |
|---------|----------------------|--------|
| shared-membership-service | Basic Auth + API Key | ✅ No changes |
| shared-provider-service | Basic Auth + API Key | ✅ No changes |
| shared-reimbursement-service | Basic Auth + API Key | ✅ No changes |
| shared-utilization-service | Basic Auth + API Key | ✅ No changes |
| shared-virtualid-service | Basic Auth + API Key | ✅ No changes |
| shared-notification-service | Basic Auth + API Key | ✅ No changes |

### User-Facing Services (JWT Only)
| Service | Authentication Method | Status |
|---------|----------------------|--------|
| auth-service | JWT Only | 🔄 To be integrated |
| membership-service | JWT Only | 🔄 To be integrated |
| appointment-service | JWT Only | 🔄 To be integrated |
| claims-service | JWT Only | 🔄 To be integrated |
| communication-service | JWT Only | 🔄 To be integrated |
| facescan-service | JWT Only | 🔄 To be integrated |
| filemanagement-service | JWT Only | 🔄 To be integrated |
| loa-service | JWT Only | 🔄 To be integrated |
| reimbursement-service | JWT Only | 🔄 To be integrated |
| provider-service | JWT Only | 🔄 To be integrated |
| provider-notification-service | JWT Only | 🔄 To be integrated |

## Questions and Considerations

1. **Should shared services use JWT?** (Answer: No - they use Basic Auth + API Key for service-to-service communication)
2. **How should we handle token refresh?** (Current: New token in X-Access-Token header)
3. **Should encryption be mandatory for JWT responses?** (Review with team)
4. **What is the token expiration policy?** (Current: 10 minutes, may need adjustment)
5. **How do we handle token revocation?** (May need Redis blacklist)
6. **Should we implement token refresh tokens?** (Future enhancement)
7. **How do user-facing services communicate with shared services?** (They should use Basic Auth + API Key when calling shared services)

## References

- JWT Filter: `shared/common-objects/src/main/java/com/medgo/filter/JwtRequestFilter.java`
- JWT Config: `shared/common-objects/src/main/java/com/medgo/config/JwtWebSecurityConfig.java`
- JWT Util: `shared/common-objects/src/main/java/com/medgo/jwt/JwtTokenUtil.java`
- Example SecurityConfig: `services/shared-provider-service/src/main/java/com/medgo/provider/config/SecurityConfig.java`

---

**Document Version**: 1.0  
**Last Updated**: 2024  
**Author**: AI Assistant  
**Status**: Draft - Pending Review

