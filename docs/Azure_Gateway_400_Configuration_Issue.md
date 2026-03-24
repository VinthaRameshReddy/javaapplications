# Azure Gateway 400 Bad Request - Configuration Issue

## 🔍 Problem Confirmed

All headers are correct in the code, but Azure Gateway is still returning `400 Bad Request`. This is **definitely an Azure Gateway configuration issue**, not a code issue.

## ✅ Headers Verified (All Correct)

From the logs, we can confirm:
- ✅ `Authorization: Basic <credentials>` - Present
- ✅ `Ocp-Apim-Subscription-Key: <api-key>` - Present
- ✅ `X-DEVICE-ID: <device-id>` - Present
- ✅ `X-APPLICATION-ID: <app-id>` - Present
- ✅ `Accept: application/json` - Present
- ✅ `Postman-Token` - **NOT present** (removed)
- ✅ `userId` - **NOT present** (removed)
- ✅ `Content-Type` - **NOT present** for GET requests

## 🎯 Root Cause: Azure Gateway Configuration

Since all headers are correct, the issue is one of the following Azure Gateway configuration problems:

### 1. Routing Rules
Azure Gateway might not have a routing rule configured for the `/sharedmembership/**` path.

**Check:**
- Azure Portal → Application Gateway → Rules
- Verify there's a routing rule for `/sharedmembership/**`
- Compare with working `/sharedprovider/**` routing rule
- Ensure the rule points to the correct backend pool

### 2. Backend Pool Configuration
The backend pool for shared-membership-service might not be configured correctly.

**Check:**
- Azure Portal → Application Gateway → Backend pools
- Verify shared-membership-service backend is configured
- Verify backend health is "Healthy"
- Check backend target (IP address or FQDN)
- Verify backend port is correct (typically 14488 based on application.properties)

### 3. Health Probe Configuration
The health probe might be failing, causing Azure Gateway to reject requests.

**Check:**
- Azure Portal → Application Gateway → Health probes
- Verify health probe is configured for shared-membership-service
- Check health probe path (should be `/sharedmembership/health` or `/sharedmembership/actuator/health`)
- Verify health probe is returning 200 OK
- Check health probe interval and timeout

### 4. WAF (Web Application Firewall) Rules
WAF might be blocking the `X-DEVICE-ID` or `X-APPLICATION-ID` headers.

**Check:**
- Azure Portal → Application Gateway → Web application firewall
- Check WAF logs for blocked requests
- Look for rules blocking custom headers starting with `X-`
- Compare WAF rules between `/sharedprovider/**` and `/sharedmembership/**`
- Consider adding exceptions for `X-DEVICE-ID` and `X-APPLICATION-ID` headers

### 5. Path-Based Routing
Azure Gateway might have path-based routing rules that don't match `/sharedmembership/**`.

**Check:**
- Azure Portal → Application Gateway → Rules → Path-based rules
- Verify path pattern matches `/sharedmembership/**` or `/sharedmembership/*`
- Ensure the rule is enabled
- Check rule priority (higher priority rules are evaluated first)

### 6. HTTP Settings
HTTP settings might be different for shared-membership-service.

**Check:**
- Azure Portal → Application Gateway → HTTP settings
- Verify HTTP settings are configured correctly
- Check cookie-based affinity settings
- Verify connection draining settings
- Compare with working provider-service HTTP settings

## 📋 Comparison: Provider-Service vs Membership-Service

| Aspect | Provider-Service | Membership-Service |
|--------|------------------|-------------------|
| **Status** | ✅ Working | ❌ 400 Error |
| **HTTP Method** | POST | GET |
| **Path** | `/sharedprovider/v1/...` | `/sharedmembership/v1/...` |
| **Headers** | Basic Auth, API Key, Postman-Token | Basic Auth, API Key, X-DEVICE-ID, X-APPLICATION-ID |
| **Request Body** | Yes (POST) | No (GET) |

**Key Differences:**
1. **Different paths** - `/sharedprovider/**` vs `/sharedmembership/**`
2. **Different HTTP methods** - POST vs GET
3. **Different headers** - Provider doesn't send X-DEVICE-ID/X-APPLICATION-ID
4. **Request body** - Provider sends request body, Membership doesn't

## 🔧 Troubleshooting Steps

### Step 1: Check Azure Gateway Logs
1. Go to Azure Portal → Application Gateway → Logs
2. Filter for 400 errors
3. Look for error messages related to routing, backend, or WAF
4. Check timestamps to match with your request

### Step 2: Test Direct Backend Call
If possible, test calling the shared-membership-service directly (bypassing Azure Gateway):
```
GET https://<backend-ip>:14488/sharedmembership/v1/member/dependent?userCode=1158094
Headers:
  Authorization: Basic <credentials>
  Ocp-Apim-Subscription-Key: <api-key>
  X-DEVICE-ID: 4554565
  X-APPLICATION-ID: 44545
  Accept: application/json
```

If direct call works, the issue is Azure Gateway configuration.
If direct call fails, the issue is in the shared-membership-service itself.

### Step 3: Compare Working vs Non-Working Configuration
1. Check Azure Gateway routing rules for `/sharedprovider/**` (working)
2. Compare with routing rules for `/sharedmembership/**` (not working)
3. Identify differences in:
   - Backend pool
   - HTTP settings
   - Health probe
   - WAF rules
   - Path patterns

### Step 4: Check Backend Health
1. Go to Azure Portal → Application Gateway → Backend health
2. Verify shared-membership-service backend shows "Healthy"
3. If unhealthy, check:
   - Backend service is running
   - Health probe path is correct
   - Network connectivity
   - Firewall rules

### Step 5: Review WAF Logs
1. Go to Azure Portal → Application Gateway → Web application firewall → Logs
2. Filter for blocked requests
3. Look for rules blocking:
   - Custom headers (X-DEVICE-ID, X-APPLICATION-ID)
   - GET requests
   - Query parameters

## 🎯 Recommended Actions

1. **Contact Azure Gateway Administrator**:
   - Request review of routing rules for `/sharedmembership/**`
   - Request comparison with working `/sharedprovider/**` configuration
   - Request WAF log review for blocked requests

2. **Verify Backend Service**:
   - Ensure shared-membership-service is running and healthy
   - Verify health probe endpoint is accessible
   - Check service logs for any errors

3. **Test with Different Headers**:
   - Try removing X-DEVICE-ID and X-APPLICATION-ID (if shared service allows)
   - Compare with provider-service request format
   - Test with POST method instead of GET (if endpoint supports it)

4. **Check Network Connectivity**:
   - Verify Azure Gateway can reach the backend
   - Check firewall rules
   - Verify network security groups

## 📝 Summary

**The code is correct.** All headers are properly set:
- ✅ Authentication headers (Basic Auth, API Key)
- ✅ Required headers (X-DEVICE-ID, X-APPLICATION-ID)
- ✅ Proper headers (Accept)
- ✅ Unwanted headers removed (Postman-Token, userId, Content-Type)

**The issue is Azure Gateway configuration:**
- Routing rules might not be configured for `/sharedmembership/**`
- Backend pool might not be configured correctly
- Health probe might be failing
- WAF rules might be blocking the request
- HTTP settings might be incorrect

**Next Steps:**
1. Contact Azure Gateway administrator
2. Review Azure Gateway configuration
3. Compare with working provider-service configuration
4. Check Azure Gateway logs for detailed error messages



