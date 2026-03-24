# JWT Integration - Service Categorization

## Overview
This document clearly categorizes all services based on their authentication requirements.

## Service Categories

### Category A: Shared Services (Basic Auth + API Key)
**Status**: ✅ **NO CHANGES REQUIRED**

These services are used for service-to-service communication and will continue using Basic Authentication + API Key validation.

| Service Name | Authentication Method | Status | Notes |
|-------------|----------------------|--------|-------|
| shared-membership-service | Basic Auth + API Key | ✅ No changes | Service-to-service |
| shared-provider-service | Basic Auth + API Key | ✅ No changes | Service-to-service |
| shared-reimbursement-service | Basic Auth + API Key | ✅ No changes | Service-to-service |
| shared-utilization-service | Basic Auth + API Key | ✅ No changes | Service-to-service |
| shared-virtualid-service | Basic Auth + API Key | ✅ No changes | Service-to-service |
| shared-notification-service | Basic Auth + API Key | ✅ No changes | Service-to-service |

**Action Required**: None - These services will continue using existing SecurityConfig with Basic Auth + API Key.

---

### Category B: User-Facing Services (JWT Only)
**Status**: 🔄 **JWT INTEGRATION REQUIRED**

These services are user-facing and will use JWT authentication only. Basic Auth and API Key will be removed.

| Service Name | Authentication Method | Status | Notes |
|-------------|----------------------|--------|-------|
| auth-service | JWT Only | 🔄 To be integrated | Special: Token generation endpoint |
| membership-service | JWT Only | 🔄 To be integrated | User-facing |
| appointment-service | JWT Only | 🔄 To be integrated | User-facing |
| claims-service | JWT Only | 🔄 To be integrated | User-facing |
| communication-service | JWT Only | 🔄 To be integrated | User-facing |
| facescan-service | JWT Only | 🔄 To be integrated | User-facing |
| filemanagement-service | JWT Only | 🔄 To be integrated | User-facing |
| loa-service | JWT Only | 🔄 To be integrated | User-facing |
| reimbursement-service | JWT Only | 🔄 To be integrated | User-facing |
| provider-service | JWT Only | 🔄 To be integrated | User-facing |
| provider-notification-service | JWT Only | 🔄 To be integrated | User-facing |

**Action Required**: 
1. Remove Basic Auth from SecurityConfig
2. Remove API Key Filter from SecurityConfig
3. Add JWT Filter to SecurityConfig
4. Configure bypass URLs
5. Update application properties
6. Test JWT validation

---

## Authentication Flow Comparison

### Shared Services Flow
```
Client Request
    ↓
Basic Authentication (HTTP Basic)
    ↓
API Key Validation
    ↓
Controller
```

### User-Facing Services Flow (After Integration)
```
Client Request
    ↓
JWT Token Validation (Authorization: Bearer <token>)
    ↓
Controller
```

---

## Integration Checklist

### Shared Services
- [x] Verify Basic Auth is configured
- [x] Verify API Key Filter is configured
- [x] No changes needed
- [ ] Document that shared services use Basic Auth + API Key

### User-Facing Services
- [ ] Remove Basic Auth from SecurityConfig
- [ ] Remove API Key Filter from SecurityConfig
- [ ] Add JWT Filter to SecurityConfig
- [ ] Configure bypass URLs
- [ ] Add JWT configuration properties
- [ ] Test JWT validation
- [ ] Verify Basic Auth is disabled
- [ ] Verify API Key is disabled
- [ ] Update API documentation

---

## Service Communication Pattern

### User-Facing Service → Shared Service
When a user-facing service needs to call a shared service:
1. User-facing service uses JWT for user authentication
2. User-facing service uses Basic Auth + API Key when calling shared services
3. Shared service validates Basic Auth + API Key (no JWT)

### Example Flow
```
User Request → User-Facing Service (JWT) → Shared Service (Basic Auth + API Key)
```

---

## Special Cases

### auth-service
- **Token Generation Endpoint**: `/api/v1/generateToken` must be in bypass URLs
- **Reason**: This endpoint generates tokens, so it cannot require JWT
- **Other Endpoints**: May require different authentication or remain public

### facescan-service
- **Public Endpoints**: Some endpoints may be public (eligibility, acceptTnc, etc.)
- **Bypass URLs**: Configure service-specific bypass URLs

---

## Migration Priority

### Phase 1: Pilot (Week 2)
1. **membership-service** - Start with one user-facing service
2. Test thoroughly
3. Document findings

### Phase 2: Core Services (Weeks 3-4)
1. **auth-service** - Token generation service
2. **appointment-service** - High-traffic service
3. **claims-service** - High-traffic service
4. **facescan-service** - Special bypass URLs

### Phase 3: Remaining Services (Week 4)
1. **communication-service**
2. **filemanagement-service**
3. **loa-service**
4. **reimbursement-service**
5. **provider-service**
6. **provider-notification-service**

---

## Testing Requirements

### Shared Services
- [x] Verify Basic Auth works
- [x] Verify API Key validation works
- [x] No JWT testing needed

### User-Facing Services
- [ ] Test JWT validation with valid tokens
- [ ] Test JWT validation with invalid tokens
- [ ] Test JWT validation with expired tokens
- [ ] Test bypass URLs work correctly
- [ ] Test error handling
- [ ] Verify Basic Auth is disabled
- [ ] Verify API Key is disabled
- [ ] Test token refresh mechanism

---

## Configuration Summary

### Shared Services Configuration
```properties
# No JWT configuration needed
# Keep existing Basic Auth and API Key configuration
```

### User-Facing Services Configuration
```properties
# JWT Configuration
jwt.enabled=true
jwt.secret-key=${JWT_SECRET_KEY}
jwt.token-validity=600000

# Bypass URLs
jwt.security.bypass.urls=/health,/actuator/health,/api/v1/generateToken

# Remove Basic Auth and API Key configuration
```

---

## References

- **Detailed Plan**: `docs/JWT_Integration_Plan.md`
- **Quick Reference**: `docs/JWT_Integration_Quick_Reference.md`
- **JWT Filter**: `shared/common-objects/src/main/java/com/medgo/filter/JwtRequestFilter.java`
- **Example SecurityConfig**: `services/shared-provider-service/src/main/java/com/medgo/provider/config/SecurityConfig.java`

---

**Last Updated**: 2024  
**Version**: 1.0  
**Status**: Ready for Implementation


















