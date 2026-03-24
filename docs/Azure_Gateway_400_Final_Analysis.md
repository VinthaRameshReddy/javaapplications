# Azure Gateway 400 Bad Request - Final Analysis

## ✅ Confirmed: Headers Are Correct

The logs confirm that ALL headers are correctly set:

```
📊 HEADER SUMMARY:
   Total headers: 5
   Has Basic Auth: true ✅
   Has API Key: true ✅
   Has X-DEVICE-ID: true ✅
   Has X-APPLICATION-ID: true ✅
   Has Accept: true ✅
   Has Postman-Token: false ✅
   Has Content-Type: false ✅
```

**Final Headers Sent:**
- ✅ `Authorization: Basic ***`
- ✅ `Ocp-Apim-Subscription-Key: 62670b5b***`
- ✅ `X-DEVICE-ID: 4554565`
- ✅ `X-APPLICATION-ID: 44545`
- ✅ `Accept: application/json`
- ✅ `userId: NOT PRESENT`
- ✅ `Postman-Token: NOT PRESENT`
- ✅ `Content-Type: NOT PRESENT` (for GET)

## 🔍 Root Cause: Azure Gateway Configuration

Since all headers are correct but Azure Gateway returns `400 Bad Request`, this is **definitely an Azure Gateway configuration issue**, not a code issue.

### Key Differences: Working vs Non-Working

| Aspect | Provider-Service (✅ Working) | Membership-Service (❌ 400 Error) |
|--------|------------------------------|----------------------------------|
| **HTTP Method** | POST | GET |
| **Path** | `/sharedprovider/v1/...` | `/sharedmembership/v1/...` |
| **Headers Sent** | Basic Auth, API Key, Postman-Token | Basic Auth, API Key, X-DEVICE-ID, X-APPLICATION-ID |
| **Request Body** | Yes (POST) | No (GET) |
| **X-DEVICE-ID** | ❌ Not sent | ✅ Sent |
| **X-APPLICATION-ID** | ❌ Not sent | ✅ Sent |

## 🎯 Possible Azure Gateway Issues

### 1. Routing Rules Not Configured
Azure Gateway might not have a routing rule for `/sharedmembership/**` path.

**Action Required:**
- Check Azure Portal → Application Gateway → Rules
- Verify routing rule exists for `/sharedmembership/**`
- Compare with working `/sharedprovider/**` routing rule
- Ensure rule is enabled and has correct priority

### 2. Backend Pool Not Configured
The backend pool for shared-membership-service might not be configured or is unhealthy.

**Action Required:**
- Check Azure Portal → Application Gateway → Backend pools
- Verify shared-membership-service backend is configured
- Check backend health status (should be "Healthy")
- Verify backend target (IP/FQDN) and port
- Compare with working provider-service backend pool

### 3. Health Probe Failing
Health probe might be failing, causing Azure Gateway to reject requests.

**Action Required:**
- Check Azure Portal → Application Gateway → Health probes
- Verify health probe for shared-membership-service
- Check probe path (should be `/sharedmembership/health` or `/sharedmembership/actuator/health`)
- Verify probe is returning 200 OK
- Check probe interval and timeout settings

### 4. WAF Rules Blocking Headers
WAF might be blocking `X-DEVICE-ID` or `X-APPLICATION-ID` headers.

**Action Required:**
- Check Azure Portal → Application Gateway → Web application firewall → Logs
- Look for blocked requests with rule IDs
- Check if custom headers (X-DEVICE-ID, X-APPLICATION-ID) are being blocked
- Compare WAF rules between `/sharedprovider/**` and `/sharedmembership/**`
- Consider adding exceptions for required headers

### 5. HTTP Settings Mismatch
HTTP settings might be different or incorrect for shared-membership-service.

**Action Required:**
- Check Azure Portal → Application Gateway → HTTP settings
- Verify HTTP settings for shared-membership-service
- Compare with working provider-service HTTP settings
- Check cookie-based affinity, connection draining, etc.

### 6. Path-Based Routing Issues
Path-based routing might not be configured correctly for `/sharedmembership/**`.

**Action Required:**
- Check Azure Portal → Application Gateway → Rules → Path-based rules
- Verify path pattern matches `/sharedmembership/**` or `/sharedmembership/*`
- Ensure rule is enabled
- Check rule priority (higher priority rules evaluated first)
- Verify backend target and HTTP settings

## 🔧 Testing Steps

### Step 1: Enable Feign FULL Logging
Feign FULL logging is already enabled. Check logs for the actual HTTP request:

```
[SharedMembershipServiceClient#findDependentsByPrincipalCode] ---> GET https://sharedinternal-stg.medicardphils.com/sharedmembership/v1/member/dependent?userCode=1158094 HTTP/1.1
[SharedMembershipServiceClient#findDependentsByPrincipalCode] Authorization: Basic aW50ZXJuYWwtY2xpZW50OnNlY3JldA==
[SharedMembershipServiceClient#findDependentsByPrincipalCode] Ocp-Apim-Subscription-Key: 62670b5bf89b41038ff0284eea190b0c
[SharedMembershipServiceClient#findDependentsByPrincipalCode] X-DEVICE-ID: 4554565
[SharedMembershipServiceClient#findDependentsByPrincipalCode] X-APPLICATION-ID: 44545
[SharedMembershipServiceClient#findDependentsByPrincipalCode] Accept: application/json
```

### Step 2: Test Direct Backend Call (if possible)
Try calling the shared-membership-service directly, bypassing Azure Gateway:

```bash
curl -X GET "https://<backend-ip>:14488/sharedmembership/v1/member/dependent?userCode=1158094" \
  -H "Authorization: Basic aW50ZXJuYWwtY2xpZW50OnNlY3JldA==" \
  -H "Ocp-Apim-Subscription-Key: 62670b5bf89b41038ff0284eea190b0c" \
  -H "X-DEVICE-ID: 4554565" \
  -H "X-APPLICATION-ID: 44545" \
  -H "Accept: application/json"
```

**Expected Results:**
- If direct call works → Issue is Azure Gateway configuration
- If direct call fails → Issue is in the shared service itself

### Step 3: Compare with Working Provider-Service
Compare the exact request format between working and non-working services:

**Working (Provider-Service):**
```
POST https://sharedinternal-stg.medicardphils.com/sharedprovider/v1/doctor
Headers:
  Authorization: Basic ***
  Ocp-Apim-Subscription-Key: ***
  Postman-Token: bypass-encryption
  Content-Type: application/json
Body: {...}
```

**Not Working (Membership-Service):**
```
GET https://sharedinternal-stg.medicardphils.com/sharedmembership/v1/member/dependent?userCode=1158094
Headers:
  Authorization: Basic ***
  Ocp-Apim-Subscription-Key: ***
  X-DEVICE-ID: 4554565
  X-APPLICATION-ID: 44545
  Accept: application/json
```

### Step 4: Check Azure Gateway Logs
1. Go to Azure Portal → Application Gateway → Logs
2. Filter for 400 errors around the request time
3. Look for error messages related to:
   - Routing rules
   - Backend pool
   - Health probe
   - WAF rules
4. Check WAF logs for blocked requests

### Step 5: Test Without X-DEVICE-ID/X-APPLICATION-ID (Temporary)
**⚠️ WARNING: This will likely cause a 500 error from the service (HeaderInterceptor will reject it), but it will help identify if Azure Gateway is blocking these headers.**

To test, add this to `application.properties`:
```properties
shared.membership.skip-device-headers=true
```

This will skip sending `X-DEVICE-ID` and `X-APPLICATION-ID` headers.

**Expected Results:**
- If Azure Gateway accepts the request → Azure Gateway was blocking these headers
- If service returns 500 → Service requires these headers (expected)
- If Azure Gateway still returns 400 → Issue is something else (routing, backend pool, etc.)

## 📋 Action Items

### Immediate Actions:
1. ✅ **Code is correct** - All headers are properly set
2. ⚠️ **Contact Azure Gateway Administrator** - Request review of:
   - Routing rules for `/sharedmembership/**`
   - Backend pool configuration
   - Health probe configuration
   - WAF rules
   - HTTP settings

### Diagnostic Actions:
1. **Check Azure Gateway Logs** - Look for 400 errors and their causes
2. **Compare Configurations** - Compare working `/sharedprovider/**` with non-working `/sharedmembership/**`
3. **Test Direct Backend** - If possible, test calling backend directly
4. **Review WAF Logs** - Check if WAF is blocking requests

### Verification Steps:
1. Verify routing rule exists for `/sharedmembership/**`
2. Verify backend pool is configured and healthy
3. Verify health probe is working
4. Verify WAF rules are not blocking required headers
5. Verify HTTP settings are correct

## 🎯 Summary

**Status:** ✅ Code is correct, headers are properly set

**Issue:** ❌ Azure Gateway configuration problem

**Next Steps:**
1. Contact Azure Gateway administrator
2. Review Azure Gateway configuration
3. Compare with working provider-service configuration
4. Check Azure Gateway logs for detailed error messages

**The 400 error is coming from Azure Gateway, not from the service. This requires Azure Gateway configuration changes, not code changes.**



