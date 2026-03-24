# JWT Header Flow Explanation

## 🔐 Understanding the Two Different Flows

### ✅ Flow 1: Client → Membership Service (JWT Validation)

**Purpose:** Validate the JWT token from the client

**Required Headers:**
- `Authorization: Bearer <jwt-token>` - JWT token to validate
- `userId: <user-id>` - Must match the token's subject (`sub`)

**What Happens:**
1. `JwtRequestFilter` intercepts the incoming request
2. Extracts `userId` header from the request
3. Extracts JWT token from `Authorization` header
4. Validates that `userId` header matches the token's subject
5. Validates token signature and expiration
6. If valid, sets authentication in SecurityContext
7. Request proceeds to controller

**Code Location:** `JwtRequestFilter.doFilterInternal()` (lines 76-103)

```java
// Check for userId header (REQUIRED for JWT validation)
String headerUserId = request.getHeader("userId");
if (!StringUtils.hasText(headerUserId)) {
    log.warn("Missing mandatory header: userId for URI: {}", requestURI);
    writeErrorResponse(response, "Missing mandatory header: userId", 401);
    return;
}

// Validate token and userId match
boolean isTokenValid = jwtTokenUtil.validateToken(jwtToken, userDetails) 
        && headerUserId.equals(tokenUserId);
```

**✅ IMPORTANT:** This validation happens **BEFORE** any Feign calls are made.

---

### ✅ Flow 2: Membership Service → Shared Service (Feign Client)

**Purpose:** Authenticate with the shared service using Basic Auth

**Required Headers:**
- `Authorization: Basic <credentials>` - Basic Auth for shared service
- `Ocp-Apim-Subscription-Key: <api-key>` - API Key for shared service
- `X-DEVICE-ID: <device-id>` - Required by shared service's HeaderInterceptor
- `X-APPLICATION-ID: <app-id>` - Required by shared service's HeaderInterceptor

**Headers to NOT Send:**
- ❌ `userId` - Shared service doesn't need it (causes 400 error)
- ❌ `Authorization: Bearer <jwt>` - Shared service uses Basic Auth, not JWT

**What Happens:**
1. Controller makes a Feign client call to shared service
2. `FeignClientConfig` interceptors run:
   - **Order 1:** Remove `userId` header ❌
   - **Order 1:** Remove JWT `Authorization` header ❌
   - **Order 2:** Add Basic Auth `Authorization` header ✅
   - **Order 3:** Add API Key header ✅
   - **Order 4:** Propagate `X-DEVICE-ID`, `X-APPLICATION-ID`, etc. ✅
3. Request is sent to shared service with correct headers

**Code Location:** `FeignClientConfig.removeUserIdHeaderInterceptor()` and other interceptors

---

## 🎯 Key Points

### 1. JWT Validation Happens FIRST
- JWT validation occurs in `JwtRequestFilter` **before** the controller is called
- The `userId` header is **required** for this validation
- This validation happens **before** any Feign calls are made

### 2. Feign Calls Happen AFTER JWT Validation
- After JWT validation passes, the controller processes the request
- The controller may make Feign calls to shared services
- At this point, we **remove** `userId` from Feign requests because:
  - Shared service doesn't need it
  - Shared service uses Basic Auth, not JWT
  - Sending `userId` causes 400 errors from Azure Gateway

### 3. Two Different Authentication Mechanisms
- **Membership Service:** Uses JWT authentication (requires `userId` header)
- **Shared Service:** Uses Basic Auth + API Key (does NOT need `userId` header)

---

## 📋 Complete Request Flow

### Step 1: Client Request to Membership Service
```
GET /member-service/api/v1/membership/dependent?userCode=1158094

Headers:
  Authorization: Bearer eyJhbGciOiJIUzUxMiJ9...  ✅ Required for JWT validation
  userId: test-user-123                          ✅ Required for JWT validation
  X-DEVICE-ID: 4554565                           ✅ Used by MDCFilter
  X-APPLICATION-ID: app-id                       ✅ Used by MDCFilter
```

### Step 2: JWT Validation (in Membership Service)
```
JwtRequestFilter:
  1. ✅ Checks userId header is present
  2. ✅ Extracts JWT token from Authorization header
  3. ✅ Validates userId matches token subject
  4. ✅ Validates token signature and expiration
  5. ✅ Sets authentication in SecurityContext
  6. ✅ Request proceeds to controller
```

### Step 3: Controller Processes Request
```
MembershipProxyController.findDependentsByPrincipalCode():
  1. ✅ Request is authenticated (JWT validation passed)
  2. ✅ Controller calls Feign client
```

### Step 4: Feign Client Call to Shared Service
```
FeignClientConfig interceptors:
  1. ✅ Remove userId header (Order 1)
  2. ✅ Remove JWT Authorization header (Order 1)
  3. ✅ Add Basic Auth Authorization header (Order 2)
  4. ✅ Add API Key header (Order 3)
  5. ✅ Propagate X-DEVICE-ID, X-APPLICATION-ID (Order 4)
  6. ✅ Add Postman-Token header (Order 5)

Final Headers Sent to Shared Service:
  Authorization: Basic aW50ZXJuYWwtY2xpZW50OnNlY3JldA==  ✅ Basic Auth
  Ocp-Apim-Subscription-Key: 62670b5b...                ✅ API Key
  X-DEVICE-ID: 4554565                                  ✅ Required
  X-APPLICATION-ID: app-id                              ✅ Required
  Postman-Token: bypass-encryption                      ✅ Encryption bypass
  ❌ userId: NOT SENT (removed by interceptor)
  ❌ Authorization: Bearer <jwt>: NOT SENT (removed by interceptor)
```

---

## ❓ FAQ

### Q: Will JWT validation work if we don't send userId to the shared service?
**A:** YES! JWT validation happens **before** the Feign call. The `userId` header is:
- ✅ **Required** in the incoming request to membership-service (for JWT validation)
- ❌ **Removed** from the outgoing Feign request to shared service (because shared service doesn't need it)

### Q: Why do we remove userId from Feign requests?
**A:** Because:
1. Shared service doesn't need it (it uses Basic Auth, not JWT)
2. Sending `userId` to shared service causes 400 errors from Azure Gateway
3. Shared service's `HeaderInterceptor` requires `X-DEVICE-ID` and `X-APPLICATION-ID`, not `userId`

### Q: What happens if the client doesn't send userId?
**A:** The `JwtRequestFilter` will reject the request with a 401 error:
```json
{
    "status": "error",
    "errorCode": "INVALID_JWT",
    "message": "Missing mandatory header: userId",
    "code": 401
}
```

### Q: What happens if we send userId to the shared service?
**A:** Azure Gateway returns a 400 Bad Request error, and the shared service rejects the request.

---

## ✅ Summary

1. **JWT validation requires `userId` header** in the incoming request to membership-service
2. **JWT validation happens BEFORE Feign calls** are made
3. **Feign interceptors remove `userId`** from outgoing requests to shared service
4. **Shared service requires `X-DEVICE-ID` and `X-APPLICATION-ID`**, not `userId`
5. **Two different authentication mechanisms:**
   - Membership Service: JWT (requires `userId`)
   - Shared Service: Basic Auth + API Key (does NOT need `userId`)

The current implementation is **correct** - `userId` is required for JWT validation in membership-service, but removed from Feign requests to shared service.



