# Azure Gateway 400 Bad Request - Fix Summary

## 🔍 Problem

Getting `400 Bad Request` from Azure Application Gateway when calling shared-membership-service, even though all headers appear correct in the logs.

## ✅ Changes Made

### 1. Removed Postman-Token Header
- Azure Gateway might reject requests with `Postman-Token` header
- Provider-service adds it, but shared-membership-service might have different gateway rules
- Changed `postmanTokenInterceptor` to `headerCleanupInterceptor` that **removes** `Postman-Token`

### 2. Added Accept Header
- Ensured `Accept: application/json` header is set for GET requests
- Some gateways require explicit Accept header

### 3. Removed Content-Type for GET
- GET requests shouldn't have `Content-Type` header
- Azure Gateway might reject GET requests with Content-Type

### 4. Enhanced Logging
- Updated logging to warn if `Postman-Token` or `Content-Type` are present in final request
- Helps identify if headers are being added after our interceptors

## 📋 Expected Headers (After Restart)

### ✅ Headers That Should Be Present:
- `Authorization: Basic <credentials>`
- `Ocp-Apim-Subscription-Key: <api-key>`
- `X-DEVICE-ID: <device-id>`
- `X-APPLICATION-ID: <app-id>`
- `Accept: application/json`
- `userId: NOT PRESENT` ✅

### ❌ Headers That Should NOT Be Present:
- `Postman-Token` ❌ (removed)
- `Content-Type` ❌ (removed for GET requests)
- `Authorization: Bearer <jwt>` ❌ (removed, replaced with Basic Auth)
- `userId` ❌ (removed)

## 🔄 Next Steps

1. **Restart the membership-service** to apply the changes
2. **Test the request** again
3. **Check the logs** to verify headers are correct:
   - Should NOT see `Postman-Token` in final headers
   - Should see `Accept: application/json`
   - Should NOT see `Content-Type` for GET requests

## 🎯 If Still Getting 400 After Restart

If the 400 error persists after restarting with the new code, the issue is likely **Azure Gateway configuration**, not code:

### Possible Azure Gateway Issues:

1. **Routing Rules**:
   - Azure Gateway might not have a routing rule for `/sharedmembership/**`
   - Check if `/sharedprovider/**` works but `/sharedmembership/**` doesn't
   - Verify backend pool is configured for shared-membership-service

2. **WAF Rules**:
   - Web Application Firewall might be blocking certain headers
   - Check Azure Gateway WAF logs for blocked requests
   - Verify if `X-DEVICE-ID` or `X-APPLICATION-ID` are triggering WAF rules

3. **Backend Health**:
   - Check if the shared-membership-service backend is healthy
   - Verify health probe configuration
   - Check if the service is reachable from Azure Gateway

4. **Header Validation**:
   - Azure Gateway might have custom header validation rules
   - Check if certain headers are required/forbidden for this path
   - Compare with working provider-service configuration

## 🔍 Debugging Steps

1. **Check Service Logs**:
   ```
   Look for: "🔍 FINAL Feign Request Headers (before HTTP request):"
   Verify: Postman-Token is NOT present
   Verify: Accept header is present
   Verify: Content-Type is NOT present for GET
   ```

2. **Check Azure Gateway Logs**:
   - Access Azure Portal → Application Gateway → Logs
   - Look for 400 errors and their causes
   - Check WAF logs if WAF is enabled

3. **Compare with Provider-Service**:
   - Provider-service uses POST requests (might have different routing)
   - Provider-service doesn't send `X-DEVICE-ID` / `X-APPLICATION-ID`
   - Check if provider-service works and compare configurations

4. **Test Directly** (if possible):
   - Try calling the shared-membership-service directly (bypassing Azure Gateway)
   - If direct call works, issue is Azure Gateway configuration
   - If direct call fails, issue is in the service itself

## 📝 Key Differences: Provider-Service vs Membership-Service

| Aspect | Provider-Service | Membership-Service |
|--------|------------------|-------------------|
| HTTP Method | POST | GET |
| Headers Sent | Basic Auth, API Key, Postman-Token | Basic Auth, API Key, X-DEVICE-ID, X-APPLICATION-ID |
| Path | `/sharedprovider/v1/...` | `/sharedmembership/v1/...` |
| Status | ✅ Working | ❌ 400 Error |

This suggests Azure Gateway might have different routing/WAF rules for different paths.



