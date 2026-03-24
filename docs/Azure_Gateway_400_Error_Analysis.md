# Azure Application Gateway 400 Bad Request - Analysis

## 🔍 Problem

Getting `400 Bad Request` from **Azure Application Gateway** (not from the shared service itself) when calling:
```
GET https://sharedinternal-stg.medicardphils.com/sharedmembership/v1/member/dependent?userCode=1158094
```

## ✅ What We've Verified

1. **Headers are Correct**:
   - ✅ `Accept: application/json`
   - ✅ `Authorization: Basic aW50ZXJuYWwtY2xpZW50OnNlY3JldA==` (internal-client:secret)
   - ✅ `Ocp-Apim-Subscription-Key: 62670b5bf89b41038ff0284eea190b0c`
   - ✅ No `userId` header
   - ✅ No `X-DEVICE-ID`, `X-APPLICATION-ID` headers
   - ✅ No `Content-Type` for GET request
   - ✅ No `Postman-Token` header

2. **Credentials are Correct**:
   - ✅ Basic Auth: `internal-client` / `secret`
   - ✅ API Key: `62670b5bf89b41038ff0284eea190b0c`
   - ✅ API Key Header: `Ocp-Apim-Subscription-Key`

3. **Request Format is Correct**:
   - ✅ GET request
   - ✅ Query parameter: `userCode=1158094`
   - ✅ URL path: `/sharedmembership/v1/member/dependent`

## ❌ Error Response

```
Status Code: 400 Bad Request
Server: Microsoft-Azure-Application-Gateway/v2
Response Body: HTML error page from Azure Gateway
```

**Important**: The error is coming from Azure Application Gateway, NOT from the shared service. This means the request is being rejected by the gateway before it reaches the backend service.

## 🎯 Root Cause: Azure Gateway Configuration

Since the request format, headers, and credentials are all correct, the issue is likely one of the following:

### 1. **Backend Routing Rules**
Azure Application Gateway might not have a routing rule configured for the path `/sharedmembership/v1/member/dependent`. The gateway needs:
- **Path-based routing rule** that matches `/sharedmembership/**`
- **Backend pool** configured for the shared-membership-service
- **Health probe** configured for the backend

### 2. **WAF (Web Application Firewall) Rules**
Azure Gateway WAF might have rules that are blocking the request:
- **Request size limits**: The request might exceed size limits
- **Header validation rules**: WAF might reject requests with certain header combinations
- **Path-based rules**: WAF might have rules that block `/sharedmembership/**` paths
- **Rate limiting**: The request might be hitting rate limits

### 3. **SSL/TLS Configuration**
- **SNI (Server Name Indication)**: The gateway might require specific SNI configuration
- **Certificate validation**: The gateway might be validating certificates incorrectly
- **TLS version**: The gateway might require specific TLS versions

### 4. **Host Header**
Azure Gateway might require the `Host` header to match a specific value. Feign should set this automatically, but we should verify it's set correctly.

## 🔍 Debugging Steps

### Step 1: Check Azure Gateway Logs

Check Azure Application Gateway logs in Azure Portal:
1. Go to Azure Portal → Application Gateway → Logs
2. Look for entries around the time of the request
3. Check for:
   - WAF block rules
   - Routing rule matches
   - Backend health probe results
   - SSL/TLS handshake errors

### Step 2: Verify Backend Pool Configuration

1. Check if the backend pool for `shared-membership-service` is configured
2. Verify the backend pool has healthy backend servers
3. Check if the health probe is passing

### Step 3: Check WAF Rules

1. Check if WAF is enabled on the gateway
2. Review WAF rules for any that might block the request
3. Check WAF logs for blocked requests

### Step 4: Compare with Working Service

Check if other services (e.g., `provider-service`, `claims-service`) are successfully calling shared services through the same gateway:
- Are they using the same URL pattern?
- Are they sending the same headers?
- Are they using the same authentication method?

## 🎯 Next Steps

### Option 1: Contact Azure/Infrastructure Team

Since this is an Azure Gateway configuration issue, contact the Azure/Infrastructure team to:
1. Verify backend routing rules for `/sharedmembership/**` paths
2. Check WAF rules for any blocking rules
3. Verify backend pool configuration
4. Check health probe status

### Option 2: Test Direct Backend Connection

If possible, test connecting directly to the shared service backend (bypassing the gateway) to verify:
1. The service is working correctly
2. The credentials are correct
3. The request format is correct

### Option 3: Check Gateway Configuration

Review Azure Application Gateway configuration:
1. **Listeners**: Check if there's a listener for `sharedinternal-stg.medicardphils.com`
2. **Routing Rules**: Check if there's a rule for `/sharedmembership/**` paths
3. **Backend Pools**: Check if the backend pool is configured and healthy
4. **Health Probes**: Check if health probes are passing

## 📋 Request Details for Azure Team

When contacting the Azure/Infrastructure team, provide:

1. **Request URL**: `https://sharedinternal-stg.medicardphils.com/sharedmembership/v1/member/dependent?userCode=1158094`
2. **Request Method**: `GET`
3. **Request Headers**:
   - `Accept: application/json`
   - `Authorization: Basic aW50ZXJuYWwtY2xpZW50OnNlY3JldA==`
   - `Ocp-Apim-Subscription-Key: 62670b5bf89b41038ff0284eea190b0c`
4. **Response**: `400 Bad Request` from Azure Gateway
5. **Timestamp**: When the request was made
6. **Source Service**: `membership-service`

## 💡 Potential Workarounds

While waiting for Azure Gateway configuration fixes:

1. **Use Different Endpoint**: If there's a direct backend URL, use it for testing
2. **Check Other Services**: Verify if other services are successfully calling shared services
3. **Review Gateway Configuration**: Check if there are different routing rules for different paths

## 🔍 Code Verification

The code is correctly:
- ✅ Removing `userId` header
- ✅ Setting Basic Auth header
- ✅ Setting API Key header
- ✅ Removing unwanted headers
- ✅ Using correct URL and path

The issue is **NOT in the code** - it's an Azure Gateway configuration issue.

---

**Status**: ⚠️ **Blocked by Azure Gateway Configuration**

**Next Action**: Contact Azure/Infrastructure team to verify gateway configuration for `/sharedmembership/**` paths.



