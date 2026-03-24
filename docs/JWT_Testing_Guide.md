# JWT Testing Guide

## Overview
This guide explains how to generate JWT tokens and test JWT authentication in the services.

## Token Generation

### Step 1: Encrypt the Request Payload

The `/api/v1/generateToken` endpoint requires an encrypted request body. First, encrypt your payload:

**Request to `/encrypt` endpoint:**
```bash
curl -X POST http://localhost:14361/auth-service/encrypt \
  -H "Content-Type: application/json" \
  -d '{"userId": "test-user-123"}'
```

**Response:**
```json
{
  "encryptedData": "<encrypted-payload>",
  "encryptedKey": "<encrypted-aes-key>",
  "iv": "<initialization-vector>"
}
```

### Step 2: Generate JWT Token

Use the encrypted payload from Step 1 to generate the token:

**Request to `/api/v1/generateToken`:**
```bash
curl -X POST http://localhost:14361/auth-service/api/v1/generateToken \
  -H "Content-Type: application/json" \
  -d '{
    "encryptedData": "<encrypted-data-from-step-1>",
    "encryptedKey": "<encrypted-key-from-step-1>",
    "iv": "<iv-from-step-1>"
  }'
```

**Response (encrypted):**
```json
{
  "statusCode": "000",
  "response": "SUCCESS",
  "data": {
    "encryptedData": "<encrypted-response>",
    "encryptedKey": "<encrypted-key>",
    "iv": "<iv>"
  },
  "traceId": "<trace-id>"
}
```

### Step 3: Decrypt the Response

Decrypt the response to get the actual token:

**Request to `/decrypt` endpoint:**
```bash
curl -X POST http://localhost:14361/auth-service/decrypt \
  -H "Content-Type: application/json" \
  -d '{
    "encryptedData": "<encrypted-data-from-step-2>",
    "encryptedKey": "<encrypted-key-from-step-2>",
    "iv": "<iv-from-step-2>"
  }'
```

**Response:**
```json
{
  "accessToken": "<jwt-token-here>"
}
```

## Complete Token Generation Script

Here's a complete bash script to generate a token:

```bash
#!/bin/bash

AUTH_SERVICE_URL="http://localhost:14361/auth-service"
USER_ID="test-user-123"

# Step 1: Encrypt the request
echo "Step 1: Encrypting request payload..."
ENCRYPTED_RESPONSE=$(curl -s -X POST "$AUTH_SERVICE_URL/encrypt" \
  -H "Content-Type: application/json" \
  -d "{\"userId\": \"$USER_ID\"}")

echo "Encrypted response: $ENCRYPTED_RESPONSE"

# Extract encrypted data (you may need to parse JSON properly)
# For simplicity, assuming you have jq installed:
ENCRYPTED_DATA=$(echo $ENCRYPTED_RESPONSE | jq -r '.encryptedData')
ENCRYPTED_KEY=$(echo $ENCRYPTED_RESPONSE | jq -r '.encryptedKey')
IV=$(echo $ENCRYPTED_RESPONSE | jq -r '.iv')

# Step 2: Generate token
echo "Step 2: Generating JWT token..."
TOKEN_RESPONSE=$(curl -s -X POST "$AUTH_SERVICE_URL/api/v1/generateToken" \
  -H "Content-Type: application/json" \
  -d "{
    \"encryptedData\": \"$ENCRYPTED_DATA\",
    \"encryptedKey\": \"$ENCRYPTED_KEY\",
    \"iv\": \"$IV\"
  }")

echo "Token response: $TOKEN_RESPONSE"

# Step 3: Decrypt response
echo "Step 3: Decrypting response..."
TOKEN_DATA=$(echo $TOKEN_RESPONSE | jq -r '.data')
DECRYPTED_TOKEN=$(curl -s -X POST "$AUTH_SERVICE_URL/decrypt" \
  -H "Content-Type: application/json" \
  -d "$TOKEN_DATA")

JWT_TOKEN=$(echo $DECRYPTED_TOKEN | jq -r '.accessToken')
echo "JWT Token: $JWT_TOKEN"
```

## Testing JWT Authentication

### Test 1: Health Endpoint (Should Work Without JWT)

```bash
curl http://localhost:11255/member-service/health
```

**Expected:** 200 OK (bypass URL, no JWT required)

### Test 2: Actuator Endpoint (Should Work Without JWT)

```bash
curl http://localhost:11255/member-service/actuator/health
```

**Expected:** 200 OK (bypass URL, no JWT required)

### Test 3: Protected Endpoint Without JWT (Should Fail)

```bash
curl http://localhost:11255/member-service/api/v1/some-endpoint
```

**Expected:** 401 Unauthorized (encrypted error response)

### Test 4: Protected Endpoint With Valid JWT (Should Work)

```bash
# Replace <JWT_TOKEN> with token from generation step
# Replace <USER_ID> with the userId used to generate token
curl -X GET http://localhost:11255/member-service/api/v1/some-endpoint \
  -H "Authorization: Bearer <JWT_TOKEN>" \
  -H "userId: <USER_ID>"
```

**Expected:** 200 OK with response data

### Test 5: Protected Endpoint With Invalid JWT (Should Fail)

```bash
curl -X GET http://localhost:11255/member-service/api/v1/some-endpoint \
  -H "Authorization: Bearer invalid-token" \
  -H "userId: test-user-123"
```

**Expected:** 401 Unauthorized (encrypted error response)

### Test 6: Protected Endpoint Without userId Header (Should Fail)

```bash
curl -X GET http://localhost:11255/member-service/api/v1/some-endpoint \
  -H "Authorization: Bearer <JWT_TOKEN>"
```

**Expected:** 401 Unauthorized - "Missing mandatory header: userId"

### Test 7: Token Refresh

When a valid token is used, the service returns a refreshed token in the `X-Access-Token` header:

```bash
curl -v -X GET http://localhost:11255/member-service/api/v1/some-endpoint \
  -H "Authorization: Bearer <JWT_TOKEN>" \
  -H "userId: <USER_ID>"
```

**Expected:** Response includes `X-Access-Token: <new-token>` header

## Service Ports

- **auth-service**: `14361` (context path: `/auth-service`)
- **membership-service**: `11255` (context path: `/member-service`)

## Quick Test Commands

### Generate Token (Simplified - if encryption is disabled for testing)

If encryption is temporarily disabled for testing, you can use:

```bash
# Direct call (if encryption is disabled)
curl -X POST http://localhost:14361/auth-service/api/v1/generateToken \
  -H "Content-Type: application/json" \
  -d '{"userId": "test-user-123"}'
```

### Test Membership Service

```bash
# 1. Test health (should work)
curl http://localhost:11255/member-service/health

# 2. Test protected endpoint (should require JWT)
curl -v http://localhost:11255/member-service/api/v1/endpoint \
  -H "Authorization: Bearer YOUR_TOKEN_HERE" \
  -H "userId: test-user-123"
```

## Troubleshooting

### Issue: "Missing mandatory header: userId"
**Solution:** Add `userId` header matching the token subject

### Issue: "Invalid JWT Token"
**Solution:** 
- Verify token is not expired
- Verify token was generated with correct secret key
- Verify userId header matches token subject

### Issue: "Token Expired"
**Solution:** Generate a new token (tokens expire after 10 minutes by default)

### Issue: Encryption/Decryption Errors
**Solution:** 
- Verify encryption keys are configured correctly
- Check that `/encrypt` and `/decrypt` endpoints are accessible
- Verify request/response format matches expected structure

## Notes

1. **Token Expiration**: Tokens expire after 10 minutes (600000ms) by default
2. **Token Refresh**: Each valid request returns a new token in `X-Access-Token` header
3. **Encryption**: Request/response encryption is enabled by default
4. **Bypass URLs**: `/health`, `/actuator/**` don't require JWT tokens
5. **Context Path**: Remember to include context path in URLs (e.g., `/member-service/...`)

---

**Last Updated**: 2024


















