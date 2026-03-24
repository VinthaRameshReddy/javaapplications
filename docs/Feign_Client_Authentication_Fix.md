# Feign Client Authentication Fix

## 🔍 Problem Identified

The membership-service was getting a `400 Bad Request` error when calling the shared membership service. The error showed:

```
feign.FeignException$BadRequest: [400 Bad Request] during [GET] to 
[https://sharedinternal-stg.medicardphils.com/sharedmembership/v1/member/dependent?userCode=1158094]
```

## ✅ Root Cause

1. **JWT Authentication Working**: The JWT filter was working correctly - requests were being authenticated and reaching the controller.

2. **Feign Client Authentication Missing**: The Feign client configuration was trying to load credentials from Azure Key Vault, but when that failed, it wasn't falling back to `application.properties` values. This meant:
   - Basic Auth credentials were `null`
   - API Key was `null`
   - No authentication headers were sent to the shared service

3. **Authorization Header Propagation Issue**: The `memberRequestInterceptor` was propagating the JWT `Authorization` header to the shared service, but the shared service expects Basic Auth, not JWT.

## 🔧 Fixes Applied

### 1. Added Fallback to application.properties

Updated `FeignClientConfig` to use `application.properties` values as fallback when Azure Key Vault fails:

```java
@Value("${shared.membership.basic.user:}")
private String defaultBasicUser;

@Value("${shared.membership.basic.password:}")
private String defaultBasicPassword;

@Value("${shared.membership.apim.header:}")
private String defaultApimHeader;

@Value("${shared.membership.apim.key:}")
private String defaultApimKey;
```

### 2. Updated loadSecrets() Method

Modified to try Azure Key Vault first, then fall back to application.properties:

```java
private void loadSecrets() {
    try {
        // Try to load from Azure Key Vault first
        String keyVaultUser = sanitize(keyVaultService.getSecretValue("shared-provider-basic-user"));
        // ... other keys
        
        // Use Key Vault values if available, otherwise fall back to application.properties
        this.basicUser = (!isEmpty(keyVaultUser)) ? keyVaultUser : defaultBasicUser;
        this.basicPassword = (!isEmpty(keyVaultPassword)) ? keyVaultPassword : defaultBasicPassword;
        this.apimHeader = (!isEmpty(keyVaultApimHeader)) ? keyVaultApimHeader : defaultApimHeader;
        this.apimKey = (!isEmpty(keyVaultApimKey)) ? keyVaultApimKey : defaultApimKey;
    } catch (Exception e) {
        // Fall back to application.properties
        this.basicUser = defaultBasicUser;
        this.basicPassword = defaultBasicPassword;
        this.apimHeader = defaultApimHeader;
        this.apimKey = defaultApimKey;
    }
}
```

### 3. Removed Authorization Header Propagation

Updated `memberRequestInterceptor` to NOT propagate the JWT `Authorization` header:

```java
@Bean(name = "memberRequestInterceptor")
public RequestInterceptor memberRequestInterceptor() {
    return requestTemplate -> {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes != null) {
            HttpServletRequest request = attributes.getRequest();
            // Propagate important headers, but NOT Authorization (JWT token)
            // Shared service uses Basic Auth, not JWT
            List<String> importantHeaders = List.of(
                    "X-DEVICE-ID",
                    "X-APPLICATION-ID",
                    "X-CLIENT-ID",
                    "X-PLATFORM"
                    // Note: Authorization header is NOT propagated
            );
            // ... propagate headers
        }
    };
}
```

## 📋 Configuration

### application.properties

The following properties are now used as fallback:

```properties
# Feign client Basic Auth
shared.membership.basic.user=internal-client
shared.membership.basic.password=secret

# API key header to send to shared service
shared.membership.apim.header=Ocp-Apim-Subscription-Key
shared.membership.apim.key=62670b5bf89b41038ff0284eea190b0c
```

## 🔐 Authentication Flow

1. **User Request** → JWT authentication (validates JWT token)
2. **Controller** → Receives authenticated request
3. **Feign Client** → Calls shared service with:
   - Basic Auth: `Authorization: Basic <base64(user:password)>`
   - API Key: `Ocp-Apim-Subscription-Key: <key>`
   - Other headers: `X-DEVICE-ID`, `X-APPLICATION-ID`, etc.
   - **NOT** the JWT token

## ✅ Expected Behavior

After the fix:

1. ✅ JWT authentication works for user requests
2. ✅ Feign client uses Basic Auth + API Key for shared service calls
3. ✅ Falls back to application.properties if Azure Key Vault is unavailable
4. ✅ JWT token is NOT sent to shared service (only Basic Auth)

## 🧪 Testing

1. **Restart the service** to pick up the configuration changes
2. **Call an endpoint** that uses the shared service:
   ```
   GET http://localhost:11255/member-service/api/v1/membership/dependent?userCode=1158094
   Headers:
     Authorization: Bearer <jwt-token>
     userId: test-user-123
   ```
3. **Verify**:
   - Request is authenticated with JWT
   - Feign client call includes Basic Auth and API Key
   - Shared service responds successfully

## 📚 Related Documentation

- `docs/Membership_Service_JWT_Request_Example.md` - JWT request examples
- `docs/JWT_Token_Usage_Guide.md` - JWT token usage guide
- `docs/JWT_Integration_Plan.md` - JWT integration overview

---

**✅ Fix Applied! Feign client now uses Basic Auth + API Key for shared service calls.**



