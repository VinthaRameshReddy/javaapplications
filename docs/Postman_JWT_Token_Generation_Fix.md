# Fix for JWT Token Generation Error

## Problem

When calling `/api/v1/generateToken` with encrypted data, you're getting this error:

```json
{
    "statusCode": "001",
    "response": "ERROR",
    "data": {
        "message": "Unrecognized field \"encryptedData\" (class com.medgo.auth.domain.request.JwtRequest)..."
    }
}
```

## Root Cause

Postman automatically adds a `Postman-Token` header to all requests. The `DecryptBodyArgumentResolver` detects this header and **skips decryption** for testing purposes. When decryption is skipped, it tries to parse the encrypted wrapper JSON directly as `JwtRequest`, which fails because `JwtRequest` only has a `userId` field.

## Solution 1: Remove Postman-Token Header (Recommended)

### Steps:
1. In Postman, open your request to `/api/v1/generateToken`
2. Go to the **Headers** tab
3. Look for `Postman-Token` header
4. **Delete** or **disable** the `Postman-Token` header
5. Send the request again

This will allow the encryption/decryption to work properly.

---

## Solution 2: Send Plain JSON (Quick Test)

If you want to test without encryption (for quick testing only):

1. **Keep** the `Postman-Token` header (or let Postman add it automatically)
2. Send **plain JSON** instead of encrypted data:

```json
{
    "userId": "test-user-123"
}
```

**Note:** This bypasses encryption and should only be used for development/testing.

---

## Solution 3: Use curl or Another Tool

If you want to test the full encryption flow without Postman's automatic headers:

```bash
# Step 1: Encrypt
curl -X POST http://localhost:14361/auth-service/encrypt \
  -H "Content-Type: application/json" \
  -d '{"userId": "test-user-123"}'

# Step 2: Generate Token (use response from Step 1)
curl -X POST http://localhost:14361/auth-service/api/v1/generateToken \
  -H "Content-Type: application/json" \
  -d '{
    "encryptedData": "FROM-STEP-1",
    "encryptedKey": "FROM-STEP-1",
    "iv": "FROM-STEP-1"
  }'

# Step 3: Decrypt (use data from Step 2)
curl -X POST http://localhost:14361/auth-service/decrypt \
  -H "Content-Type: application/json" \
  -d '{
    "encryptedData": "FROM-STEP-2-DATA",
    "encryptedKey": "FROM-STEP-2-DATA",
    "iv": "FROM-STEP-2-DATA"
  }'
```

---

## Complete Correct Flow (Postman)

### Step 1: Encrypt Request
**URL:** `POST http://localhost:14361/auth-service/encrypt`

**Headers:**
```
Content-Type: application/json
```

**Body:**
```json
{
  "userId": "test-user-123"
}
```

**Response:**
```json
{
  "encryptedData": "...",
  "encryptedKey": "...",
  "iv": "..."
}
```

---

### Step 2: Generate Token
**URL:** `POST http://localhost:14361/auth-service/api/v1/generateToken`

**Headers:**
```
Content-Type: application/json
```
**⚠️ IMPORTANT: Remove or disable the `Postman-Token` header!**

**Body:**
```json
{
  "encryptedData": "<from-step-1>",
  "encryptedKey": "<from-step-1>",
  "iv": "<from-step-1>"
}
```

**Response (Encrypted):**
```json
{
  "statusCode": "000",
  "response": "SUCCESS",
  "data": {
    "encryptedData": "...",
    "encryptedKey": "...",
    "iv": "..."
  }
}
```

---

### Step 3: Decrypt Response
**URL:** `POST http://localhost:14361/auth-service/decrypt`

**Headers:**
```
Content-Type: application/json
```

**Body:**
```json
{
  "encryptedData": "<from-step-2-data.encryptedData>",
  "encryptedKey": "<from-step-2-data.encryptedKey>",
  "iv": "<from-step-2-data.iv>"
}
```

**Response:**
```json
{
  "accessToken": "eyJhbGciOiJIUzUxMiJ9..."
}
```

---

## How to Remove Postman-Token Header in Postman

1. Open your request in Postman
2. Click on the **Headers** tab
3. You'll see a list of headers
4. Look for `Postman-Token` in the list
5. **Uncheck** the checkbox next to it, OR
6. Click the **X** button to delete it
7. Save your request

**Note:** Postman may add this header automatically again. You can:
- Disable it in Postman Settings → General → "Send Postman-Token header"
- Or manually remove it each time

---

## Summary

**The issue:** Postman's automatic `Postman-Token` header causes decryption to be skipped.

**The fix:** Remove the `Postman-Token` header from your Postman request when testing encryption/decryption.

**Alternative:** Send plain JSON `{"userId": "test-user-123"}` if you want to skip encryption for quick testing.

---

## Verification

After removing the `Postman-Token` header, you should see in the logs:
```
Decrypted payload: JwtRequest[userId=test-user-123]
```

If you still see the error, check:
1. Is `encryption.enabled=true` in `application.properties`? ✅ (It is)
2. Is the `Postman-Token` header removed? ⚠️ (Check this)
3. Are you sending the correct encrypted wrapper format? ✅ (You are)


















