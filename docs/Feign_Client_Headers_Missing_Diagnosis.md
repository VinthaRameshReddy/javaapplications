# Feign Client Headers Missing Diagnosis

## 🔍 Problem

The request is not hitting the shared service endpoints. Azure Gateway is returning a `400 Bad Request` error.

## 📋 Analysis

### 1. Header Requirements

The shared-membership-service has `HeaderInterceptor` enabled (via `InterceptorConfig` in `common-objects`), which **REQUIRES**:
- `X-DEVICE-ID` header
- `X-APPLICATION-ID` header (mapped to `TRACE_HEADER`)

If these headers are missing, the shared service will reject the request.

### 2. Current Flow

1. **Client Request to Membership Service:**
   - Client sends request with `Authorization: Bearer <jwt>` and `userId` header
   - Client may or may not send `X-DEVICE-ID` and `X-APPLICATION-ID`
   - `MDCFilter` logs show: `MDC FILTER Header deviceId : 4554565` (so `X-DEVICE-ID` is present)
   - `HeaderInterceptor` in membership-service (if enabled) would require these headers

2. **Feign Client Request to Shared Service:**
   - `FeignClientConfig` interceptors run:
     - Order 1: Remove `userId` and JWT `Authorization` headers
     - Order 2: Add Basic Auth `Authorization` header
     - Order 3: Add API Key header
     - Order 4: Propagate `X-DEVICE-ID`, `X-APPLICATION-ID`, etc.
     - Order 5: Add `Postman-Token` header
     - Order MAX: Final verification and logging

3. **Shared Service:**
   - `HeaderInterceptor` checks for `X-DEVICE-ID` and `X-APPLICATION-ID`
   - If missing, throws `ImportantHeadersMissingException`
   - This would cause a 500 error, not a 400 error

### 3. Possible Root Causes

#### A. Headers Not Propagated
- Headers are present in the incoming request but not being propagated to Feign request
- RequestContext might not have the values
- Header names might be case-sensitive

#### B. Azure Gateway Rejection
- Azure Gateway is rejecting the request before it reaches the shared service
- Azure Gateway WAF rules might be blocking the request
- Azure Gateway routing rules might require specific headers
- URL format might be incorrect

#### C. HeaderInterceptor Not Enabled in Membership Service
- If `HeaderInterceptor` is not enabled in membership-service, headers might not be validated
- But they still need to be propagated to the shared service

## 🔧 Enhanced Logging

Added comprehensive logging to diagnose the issue:

1. **Incoming Request Headers Logging:**
   - Logs all headers in the incoming request
   - Shows which headers are available for propagation

2. **RequestContext Check:**
   - Checks if `X-DEVICE-ID` and `X-APPLICATION-ID` are in `RequestContext`
   - Uses `RequestContext` values as fallback if headers are not in the request

3. **Header Propagation Logging:**
   - Logs which headers are being propagated
   - Shows warnings if required headers are missing

4. **Final Header Verification:**
   - Logs all headers in the final Feign request
   - Shows warnings if required headers are missing

## ✅ Solution Steps

### Step 1: Check Client Request Headers

Ensure the client is sending:
- `X-DEVICE-ID: <device-id>`
- `X-APPLICATION-ID: <app-id>` (or `X-APPLICATION-ID: <trace-id>`)

### Step 2: Check Logs

After making a request, check the logs for:
1. **Incoming Request Headers:** Are `X-DEVICE-ID` and `X-APPLICATION-ID` present?
2. **RequestContext Values:** Are they set in `RequestContext`?
3. **Header Propagation:** Are they being propagated to the Feign request?
4. **Final Headers:** Are they present in the final Feign request?

### Step 3: Verify HeaderInterceptor Status

Check if `HeaderInterceptor` is enabled in membership-service:
- If enabled, it should validate headers in the incoming request
- If headers are missing, the request should be rejected before reaching the controller

### Step 4: Check Azure Gateway Configuration

If headers are being sent correctly but Azure Gateway is still rejecting:
- Check Azure Gateway routing rules
- Check Azure Gateway WAF rules
- Check if the URL format is correct
- Check if Azure Gateway requires additional headers

## 🎯 Expected Log Output

### Successful Request:
```
🔍 Incoming Request Headers (for propagation):
   X-DEVICE-ID: 4554565
   X-APPLICATION-ID: app-id
   Authorization: Bearer *** (JWT - will NOT propagate)
   userId: test-user-123 (will NOT propagate)

🔍 Propagating headers to Feign request:
   ℹ️ Found X-DEVICE-ID in RequestContext: 4554565
   ℹ️ Found X-APPLICATION-ID in RequestContext: app-id
   ✅ Propagated X-DEVICE-ID: 4554565
   ✅ Propagated X-APPLICATION-ID: app-id

🔍 FINAL Feign Request Headers (before HTTP request):
   URL: https://sharedinternal-stg.medicardphils.com/sharedmembership/v1/member/dependent?userCode=1158094
   Method: GET
   ✅ Authorization: Basic ***
   ✅ Ocp-Apim-Subscription-Key: 62670b5b***
   ✅ X-DEVICE-ID: [4554565] (REQUIRED by shared service)
   ✅ X-APPLICATION-ID: [app-id] (REQUIRED by shared service)
   ✅ Postman-Token: [bypass-encryption]
   ✅ userId header: NOT PRESENT ✅
```

### Failed Request (Missing Headers):
```
🔍 Incoming Request Headers (for propagation):
   Authorization: Bearer *** (JWT - will NOT propagate)
   userId: test-user-123 (will NOT propagate)
   (X-DEVICE-ID and X-APPLICATION-ID are missing)

🔍 Propagating headers to Feign request:
   ❌ WARNING: X-DEVICE-ID is MISSING in incoming request and RequestContext!
      ⚠️ CRITICAL: This header is REQUIRED by shared service HeaderInterceptor!
      ⚠️ Request will be rejected with 400/500 error!
      ⚠️ Using default value: default-device-id (THIS MAY CAUSE ISSUES!)

🔍 FINAL Feign Request Headers (before HTTP request):
   ❌❌❌ WARNING: X-DEVICE-ID header is MISSING (REQUIRED by shared service HeaderInterceptor)!
   ❌❌❌ WARNING: X-APPLICATION-ID header is MISSING (REQUIRED by shared service HeaderInterceptor)!
```

## 📝 Next Steps

1. **Restart the membership-service** with the enhanced logging
2. **Make a test request** from the client
3. **Check the logs** to see which headers are present/missing
4. **Verify** if headers are being propagated correctly
5. **Check Azure Gateway logs** to see why it's rejecting the request
6. **Update the client** to send the required headers if they're missing

## 🔗 Related Files

- `FeignClientConfig.java`: Contains the header propagation logic
- `HeaderInterceptor.java`: Validates headers in the shared service
- `InterceptorConfig.java`: Registers `HeaderInterceptor` in services that scan `com.medgo.config`
- `RequestContext.java`: Stores `deviceId` and `traceId` in thread-local storage



