# Feign Client 403 Forbidden Fix

## 🔍 Problem

Getting `403 Forbidden` error when calling the shared membership service through Feign client.

## 🎯 Root Causes

1. **Multiple Interceptors Conflict**: Having multiple `@Primary` interceptors can cause conflicts and unpredictable execution order
2. **Header Removal Issues**: If headers are removed after they're added, authentication fails
3. **Empty Credentials**: If Basic Auth or API Key credentials are empty, the request will be rejected
4. **Interceptor Order**: The order of interceptor execution might not be guaranteed

## ✅ Solution

### 1. Combined Authentication Interceptor

Created a single `authenticationInterceptor` that handles all authentication logic in the correct order:

1. **Remove unwanted headers** (`userId`, JWT `Authorization`)
2. **Add Basic Auth header** (for shared service authentication)
3. **Add API Key header** (for shared service authorization)

### 2. Removed Duplicate Interceptors

Removed the separate `basicAuthRequestInterceptor` and `apimSubscriptionKeyInterceptor` to avoid conflicts.

### 3. Enhanced Logging

Added detailed logging to help diagnose credential loading issues:

- Shows which credentials are loaded
- Shows which credentials are missing
- Shows values from application.properties fallback

## 🔧 Changes Made

### File: `FeignClientConfig.java`

**Before:**
```java
@Bean
@Primary
public RequestInterceptor removeUnwantedHeadersInterceptor() { ... }

@Bean
@Primary
public RequestInterceptor basicAuthRequestInterceptor() { ... }

@Bean
@Primary
public RequestInterceptor apimSubscriptionKeyInterceptor() { ... }
```

**After:**
```java
@Bean
@Primary
public RequestInterceptor authenticationInterceptor() {
    return requestTemplate -> {
        // Step 1: Remove unwanted headers
        requestTemplate.removeHeader("userId");
        requestTemplate.removeHeader("Authorization");
        
        // Step 2: Add Basic Auth
        if (!isEmpty(basicUser) && !isEmpty(basicPassword)) {
            String auth = basicUser + ":" + basicPassword;
            String encodedAuth = java.util.Base64.getEncoder().encodeToString(auth.getBytes());
            requestTemplate.header("Authorization", "Basic " + encodedAuth);
        }
        
        // Step 3: Add API Key
        if (!isEmpty(apimKey) && !isEmpty(apimHeader)) {
            requestTemplate.header(apimHeader, apimKey);
        }
    };
}
```

## 📋 Header Flow

### Request to Membership Service:
```
Headers:
  Authorization: Bearer <jwt-token>  ✅ Used for JWT validation
  userId: test-user-123              ✅ Used for JWT validation
```

### Feign Client Request to Shared Service:
```
Headers (after authenticationInterceptor):
  Authorization: Basic <base64-encoded-credentials>  ✅ Basic Auth
  Ocp-Apim-Subscription-Key: <api-key>              ✅ API Key
  Postman-Token: bypass-encryption                  ✅ Set by headerPropagationInterceptor
  X-DEVICE-ID: <device-id>                          ✅ Propagated by memberRequestInterceptor
  X-APPLICATION-ID: <app-id>                        ✅ Propagated by memberRequestInterceptor
  X-CLIENT-ID: <client-id>                          ✅ Propagated by memberRequestInterceptor
  X-PLATFORM: <platform>                            ✅ Propagated by memberRequestInterceptor
  
  ❌ userId: REMOVED (not sent to shared service)
  ❌ Authorization (JWT): REMOVED (replaced with Basic Auth)
```

## 🔍 Debugging

### Check Service Logs

When the service starts, look for:

```
✅ FeignClientConfig initialized:
   Basic Auth User: internal-client
   Basic Auth Password: ***
   APIM Header: Ocp-Apim-Subscription-Key
   APIM Key: 62670b5b***
```

### If Credentials Are Missing

You'll see:
```
❌ ERROR: Basic Auth credentials are missing! Check application.properties or Azure Key Vault.
   defaultBasicUser from properties: internal-client
   defaultBasicPassword from properties: SET
```

### If Request Fails

Check the logs for:
```
❌ ERROR: Basic Auth credentials are empty! Cannot authenticate with shared service.
❌ ERROR: API Key is empty! Cannot authorize with shared service.
```

## 🧪 Testing

1. **Restart the service** to pick up the changes
2. **Check startup logs** for credential loading
3. **Make a request**:
   ```
   GET http://localhost:11255/member-service/api/v1/membership/dependent?userCode=1158094
   Headers:
     Authorization: Bearer <jwt-token>
     userId: test-user-123
   ```
4. **Verify**:
   - Request is authenticated with JWT (membership-service)
   - Feign client call includes Basic Auth and API Key
   - Shared service responds successfully (no 403 error)

## 🔐 Credentials Configuration

### application.properties

```properties
# Feign client Basic Auth
shared.membership.basic.user=internal-client
shared.membership.basic.password=secret

# API key header to send to shared service
shared.membership.apim.header=Ocp-Apim-Subscription-Key
shared.membership.apim.key=62670b5bf89b41038ff0284eea190b0c
```

### Azure Key Vault (Optional)

If Azure Key Vault is configured, it will be tried first:
- `shared-provider-basic-user`
- `shared-provider-basic-password`
- `shared-provider-apim-header`
- `shared-provider-apim-key`

If Key Vault fails, it falls back to `application.properties`.

## ⚠️ Common Issues

### Issue 1: Credentials Not Loaded

**Symptoms**: Logs show "NOT SET" for credentials

**Fix**: 
- Check `application.properties` has correct values
- Check Azure Key Vault configuration (if using)
- Verify properties are not commented out

### Issue 2: Wrong Credentials

**Symptoms**: 403 Forbidden even though credentials are set

**Fix**:
- Verify credentials are correct for the shared service
- Check if credentials have changed in the shared service
- Contact shared service team to verify credentials

### Issue 3: Multiple Interceptors Conflict

**Symptoms**: Headers are not set correctly, or headers are removed after being added

**Fix**: 
- Ensure only `authenticationInterceptor` is `@Primary`
- Remove duplicate interceptors
- Check interceptor order in Feign configuration

## 📚 Related Documentation

- `docs/Feign_Client_Authentication_Fix.md` - Initial authentication setup
- `docs/Feign_Client_Header_Fix.md` - Header removal fix
- `docs/Membership_Service_JWT_Request_Example.md` - JWT request examples

---

**✅ Fix Applied! Single authentication interceptor ensures correct header order and prevents conflicts.**



