# Feign Client 400 Bad Request Debugging Guide

## 🔍 Problem

Getting `400 Bad Request` from Azure Application Gateway when calling the shared membership service through Feign client.

## 🎯 Possible Causes

1. **Invalid Headers**: Azure Application Gateway might reject certain headers
2. **Missing Required Headers**: Gateway might require specific headers
3. **Header Format Issues**: Headers might be in wrong format
4. **Content-Type for GET**: GET requests shouldn't have Content-Type header
5. **Postman-Token Header**: This header might cause issues with Azure Gateway

## ✅ Solutions Applied

### 1. Removed Content-Type for GET Requests

GET requests don't need a `Content-Type` header, and Azure Application Gateway might reject it:

```java
if ("GET".equals(requestTemplate.method())) {
    requestTemplate.removeHeader("Content-Type"); // Remove Content-Type for GET requests
    requestTemplate.header("Accept", "application/json");
} else {
    // For POST/PUT requests, set Content-Type
    requestTemplate.header("Content-Type", "application/json");
    requestTemplate.header("Accept", "application/json");
}
```

### 2. Removed Postman-Token Header

The `Postman-Token` header is not needed for external calls and might cause Azure Gateway to reject the request:

```java
requestTemplate.removeHeader("Postman-Token"); // Remove Postman-Token (might cause 400 from Azure Gateway)
```

### 3. Enhanced Request Logging

Added logging to see exactly what's being sent:

```java
System.out.println("🔍 Feign Request to Shared Service:");
System.out.println("   Method: " + requestTemplate.method());
System.out.println("   URL: " + requestTemplate.url());
System.out.println("   Has Authorization: " + (requestTemplate.headers().get("Authorization") != null));
System.out.println("   Has API Key: " + (requestTemplate.headers().get(apimHeader) != null));
```

## 🔍 Debugging Steps

### Step 1: Check Service Logs

When you make a request, look for these log messages:

```
🔍 Feign Request to Shared Service:
   Method: GET
   URL: https://sharedinternal-stg.medicardphils.com/sharedmembership/v1/member/dependent?userCode=1158094
   Has Authorization: true
   Has API Key: true
```

### Step 2: Enable Feign Full Logging

Feign logging is already set to `FULL`:

```properties
logging.level.feign=DEBUG
```

Check the logs for the actual request being sent. Look for lines like:
```
[SharedMembershipServiceClient#findDependentsByPrincipalCode] ---> GET https://...
[SharedMembershipServiceClient#findDependentsByPrincipalCode] Authorization: Basic ...
[SharedMembershipServiceClient#findDependentsByPrincipalCode] Ocp-Apim-Subscription-Key: ...
```

### Step 3: Verify Credentials

Check startup logs for:

```
✅ FeignClientConfig initialized:
   Basic Auth User: internal-client
   Basic Auth Password: ***
   APIM Header: Ocp-Apim-Subscription-Key
   APIM Key: 62670b5b***
```

### Step 4: Check Request Headers

The request should have:
- ✅ `Authorization: Basic <base64-encoded-credentials>`
- ✅ `Ocp-Apim-Subscription-Key: <api-key>`
- ✅ `Accept: application/json`
- ❌ NO `Content-Type` header (for GET requests)
- ❌ NO `userId` header
- ❌ NO `Postman-Token` header
- ❌ NO JWT `Authorization` header

## 🔧 Current Configuration

### Headers Removed:
- `userId` - Not needed by shared service
- `Authorization` (JWT) - Replaced with Basic Auth
- `Postman-Token` - Not needed, might cause 400
- `Content-Type` (for GET requests) - Not needed for GET

### Headers Added:
- `Authorization: Basic <creds>` - For authentication
- `Ocp-Apim-Subscription-Key: <key>` - For authorization
- `Accept: application/json` - For response content type

### Headers Propagated:
- `X-DEVICE-ID` - If present in request
- `X-APPLICATION-ID` - If present in request
- `X-CLIENT-ID` - If present in request
- `X-PLATFORM` - If present in request

## 🧪 Testing

1. **Restart the service** to pick up changes
2. **Make a request** and check logs for:
   - Request URL
   - Headers being sent
   - Any error messages
3. **Verify credentials** are loaded correctly
4. **Check Feign logs** for the actual HTTP request

## ⚠️ Common Issues

### Issue 1: Credentials Not Loaded

**Symptoms**: Logs show "NOT SET" for credentials

**Fix**: 
- Check `application.properties` has correct values
- Check Azure Key Vault configuration
- Verify properties are not commented out

### Issue 2: Headers Still Present

**Symptoms**: Logs show headers that should be removed

**Fix**: 
- Check interceptor order
- Verify `removeHeader()` is being called
- Check if other interceptors are adding headers

### Issue 3: Azure Gateway Rejects Request

**Symptoms**: 400 Bad Request from Azure Application Gateway

**Possible Causes**:
- Wrong credentials
- Missing required headers
- Invalid header format
- Header values are empty/null
- URL encoding issues

**Fix**:
- Verify credentials are correct
- Check if shared service team has specific header requirements
- Test with a tool like Postman to see what works
- Check Azure Application Gateway logs (if available)

## 📋 Next Steps

1. **Restart the service** and check startup logs
2. **Make a test request** and check the Feign logs
3. **Compare** the actual request with a working request (if available)
4. **Contact shared service team** to verify:
   - Required headers
   - Required header format
   - Expected authentication method
   - API endpoint URL format

## 📚 Related Documentation

- `docs/Feign_Client_Authentication_Fix.md` - Authentication setup
- `docs/Feign_Client_Header_Fix.md` - Header removal
- `docs/Feign_Client_403_Fix.md` - 403 Forbidden fix

---

**🔍 Check the service logs after restart to see what headers are actually being sent!**



