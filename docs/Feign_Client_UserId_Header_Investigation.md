# Feign Client userId Header Investigation

## 🔍 Problem Summary

Getting `400 Bad Request` from Azure Application Gateway when calling shared-membership-service, even though:
- ✅ `userId` header is being removed in interceptors
- ✅ Logs show `Has userId header: false (should be false)`
- ✅ Basic Auth and API Key are being set correctly

## 📋 Shared Service Requirements

From `shared-membership-service/src/main/java/com/medgo/member/config/SecurityConfig.java`:

### Expected Headers:
1. **Authorization: Basic <credentials>** - Required for Basic Authentication
2. **Ocp-Apim-Subscription-Key: <key>** - Required for API Key validation
3. **Optional**: `X-DEVICE-ID`, `X-APPLICATION-ID`, `X-CLIENT-ID`, `X-PLATFORM`

### NOT Expected:
- ❌ `userId` header - NOT needed and causes 400 Bad Request
- ❌ `Authorization: Bearer <jwt>` - Shared service uses Basic Auth, not JWT

## 🔧 Current Implementation

### FeignClientConfig.java

1. **authenticationInterceptor** (Order 1):
   - Removes `userId`, `Authorization` (JWT), `Postman-Token`
   - Adds `Authorization: Basic <creds>`
   - Adds API Key header
   - Logs all headers

2. **memberRequestInterceptor** (Order 2):
   - Removes `userId` again (safety check)
   - Propagates only: `X-DEVICE-ID`, `X-APPLICATION-ID`, `X-CLIENT-ID`, `X-PLATFORM`
   - Final verification to ensure `userId` is not present

## 🐛 Possible Root Causes

### 1. Azure Application Gateway WAF Rules
Azure Application Gateway might have Web Application Firewall (WAF) rules that:
- Reject requests with unknown/unexpected headers
- Validate header format/structure
- Require specific headers to be present/absent

### 2. Feign Automatic Header Propagation
Spring Cloud OpenFeign might be automatically copying headers from `HttpServletRequest` BEFORE our interceptors run, or AFTER our interceptors run but before the request is sent.

### 3. Header Case Sensitivity
Feign/HTTP headers are case-insensitive, but the removal might not be working correctly if the header name has a different case.

### 4. Other Headers Causing Issues
There might be other headers being sent that Azure Gateway rejects (not just `userId`).

## 🔍 Debugging Steps

### Step 1: Check Feign FULL Logs

Enable FULL Feign logging to see the actual HTTP request:

```properties
logging.level.feign=DEBUG
logging.level.com.medgo.member.feign.SharedMembershipServiceClient=DEBUG
```

Look for lines like:
```
[SharedMembershipServiceClient#findDependentsByPrincipalCode] ---> GET https://...
[SharedMembershipServiceClient#findDependentsByPrincipalCode] Authorization: Basic ...
[SharedMembershipServiceClient#findDependentsByPrincipalCode] Ocp-Apim-Subscription-Key: ...
[SharedMembershipServiceClient#findDependentsByPrincipalCode] userId: ...  <-- This should NOT be present
```

### Step 2: Check Current Logs

The enhanced logging should show:
```
🔍 Feign Request to Shared Service (authenticationInterceptor):
   Method: GET
   URL: /sharedmembership/v1/member/dependent?userCode=1158094
   ALL Headers: {...}
   ✅ userId header successfully removed

🔍 Feign Request to Shared Service (memberRequestInterceptor - FINAL):
   Method: GET
   URL: /sharedmembership/v1/member/dependent?userCode=1158094
   ALL Headers (FINAL): {...}
   ✅✅✅ SUCCESS: No userId header found in final request. Request should succeed.
```

### Step 3: Verify Headers in Final Request

Check the "ALL Headers (FINAL)" log to see exactly what headers are being sent. Look for:
- ✅ `Authorization: Basic ...` (should be present)
- ✅ `Ocp-Apim-Subscription-Key: ...` (should be present)
- ❌ `userId: ...` (should NOT be present)
- ❌ Any other unexpected headers

## 🎯 Next Steps

### Option 1: Check Azure Application Gateway Configuration
1. Review Azure Application Gateway WAF rules
2. Check if there are rules that reject specific headers
3. Verify if `userId` header is in a blacklist

### Option 2: Use WireMock or HTTP Client to Test
Test the shared service directly with a simple HTTP client (curl, Postman) to verify:
- What headers are actually required
- What headers cause 400 Bad Request
- If the issue is with Azure Gateway or the service itself

### Option 3: Disable Automatic Header Propagation
If Feign is automatically copying headers, we might need to:
- Use a custom `RequestTemplate` 
- Disable Feign's automatic header propagation
- Create a custom `RequestInterceptor` that explicitly controls all headers

### Option 4: Contact Shared Service Team
Ask the shared service team:
- What headers are actually required/allowed?
- Are there Azure Gateway rules that reject certain headers?
- What causes a 400 Bad Request from Azure Gateway?

## 📝 Current Status

✅ **Fixed**: `userId` header removal in interceptors
✅ **Fixed**: Basic Auth and API Key authentication
❌ **Still Failing**: 400 Bad Request from Azure Application Gateway
🔍 **Investigation**: Need to verify actual HTTP request being sent

## 🔗 Related Files

- `services/membership-service/src/main/java/com/medgo/member/config/FeignClientConfig.java`
- `services/shared-membership-service/src/main/java/com/medgo/member/config/SecurityConfig.java`
- `services/shared-membership-service/src/main/java/com/medgo/member/controller/MemberController.java`

---

**Next Action**: Check the Feign FULL logs to see the actual HTTP request being sent, and verify if `userId` header is actually being sent despite our removal attempts.



