# Feign Client Header Fix - Remove userId Header

## 🔍 Problem

The shared membership service was receiving a `400 Bad Request` error because the `userId` header was being sent to it. The `userId` header is:
- **Required** for JWT validation in the membership-service
- **Not needed** (and causes errors) in the shared service

## ✅ Solution

Added an interceptor to explicitly remove the `userId` header (and `Authorization` header) from Feign client requests to the shared service.

## 🔧 Changes Made

### Added `removeUnwantedHeadersInterceptor`

This interceptor explicitly removes headers that should NOT be sent to the shared service:

```java
@Bean
@Primary
public RequestInterceptor removeUnwantedHeadersInterceptor() {
    return requestTemplate -> {
        // Explicitly remove headers that should not be sent to shared service
        requestTemplate.removeHeader("userId");
        requestTemplate.removeHeader("Authorization");
    };
}
```

## 📋 Header Flow

### Request to Membership Service
```
GET /member-service/api/v1/membership/dependent?userCode=1158094
Headers:
  Authorization: Bearer <jwt-token>  ✅ Used for JWT validation
  userId: test-user-123              ✅ Used for JWT validation
  X-DEVICE-ID: <device-id>           ✅ Propagated to shared service
  X-APPLICATION-ID: <app-id>         ✅ Propagated to shared service
```

### Feign Client Request to Shared Service
```
GET /sharedmembership/v1/member/dependent?userCode=1158094
Headers:
  Authorization: Basic <base64-encoded-credentials>  ✅ Set by basicAuthRequestInterceptor
  Ocp-Apim-Subscription-Key: <api-key>              ✅ Set by apimSubscriptionKeyInterceptor
  X-DEVICE-ID: <device-id>                          ✅ Propagated from request
  X-APPLICATION-ID: <app-id>                        ✅ Propagated from request
  X-CLIENT-ID: <client-id>                          ✅ Propagated from request
  X-PLATFORM: <platform>                            ✅ Propagated from request
  Postman-Token: bypass-encryption                  ✅ Set by headerPropagationInterceptor
  
  ❌ userId: REMOVED (not needed by shared service)
  ❌ Authorization (JWT): REMOVED (shared service uses Basic Auth)
```

## 🔐 Header Purposes

| Header | Membership Service | Shared Service | Action |
|--------|-------------------|----------------|--------|
| `Authorization: Bearer <jwt>` | ✅ Required for JWT validation | ❌ Not needed | **Removed** |
| `userId` | ✅ Required for JWT validation | ❌ Causes 400 error | **Removed** |
| `Authorization: Basic <creds>` | ❌ Not used | ✅ Required for authentication | **Added** by `basicAuthRequestInterceptor` |
| `Ocp-Apim-Subscription-Key` | ❌ Not used | ✅ Required for API access | **Added** by `apimSubscriptionKeyInterceptor` |
| `X-DEVICE-ID` | ✅ Optional | ✅ Optional | **Propagated** |
| `X-APPLICATION-ID` | ✅ Optional | ✅ Optional | **Propagated** |
| `X-CLIENT-ID` | ✅ Optional | ✅ Optional | **Propagated** |
| `X-PLATFORM` | ✅ Optional | ✅ Optional | **Propagated** |

## 🎯 Interceptor Order

The interceptors run in this order:

1. **removeUnwantedHeadersInterceptor** - Removes `userId` and `Authorization` (JWT)
2. **basicAuthRequestInterceptor** - Adds `Authorization: Basic <creds>`
3. **apimSubscriptionKeyInterceptor** - Adds `Ocp-Apim-Subscription-Key`
4. **memberRequestInterceptor** - Propagates specific headers (`X-DEVICE-ID`, etc.)
5. **headerPropagationInterceptor** - Adds `Postman-Token: bypass-encryption`

## ✅ Expected Behavior

After this fix:

1. ✅ User sends request with JWT token and `userId` header
2. ✅ Membership service validates JWT using `userId` header
3. ✅ Feign client removes `userId` and JWT `Authorization` headers
4. ✅ Feign client adds Basic Auth and API Key headers
5. ✅ Feign client propagates only allowed headers (`X-DEVICE-ID`, etc.)
6. ✅ Shared service receives request with correct headers
7. ✅ Shared service authenticates and returns data

## 🧪 Testing

1. **Restart the service** to pick up the changes
2. **Call an endpoint**:
   ```
   GET http://localhost:11255/member-service/api/v1/membership/dependent?userCode=1158094
   Headers:
     Authorization: Bearer <jwt-token>
     userId: test-user-123
   ```
3. **Verify**:
   - Request is authenticated with JWT (membership-service)
   - Feign client call does NOT include `userId` header
   - Feign client call includes Basic Auth and API Key
   - Shared service responds successfully (no 400 error)

## 📚 Related Documentation

- `docs/Feign_Client_Authentication_Fix.md` - Feign client authentication setup
- `docs/Membership_Service_JWT_Request_Example.md` - JWT request examples
- `docs/JWT_Token_Usage_Guide.md` - JWT token usage guide

---

**✅ Fix Applied! `userId` header is now removed from Feign client requests to shared service.**



