# Postman Environment Setup for JWT Token Generation

## Quick Fix for Encryption Errors

### Problem
Postman automatically adds a `Postman-Token` header which causes decryption to be skipped, resulting in errors when sending encrypted data.

### Solution: Disable Postman-Token Header

#### Option 1: Disable Globally in Postman Settings
1. Open Postman
2. Go to **Settings** (gear icon) → **General**
3. Find **"Send Postman-Token header"**
4. **Uncheck** this option
5. Restart Postman

#### Option 2: Remove from Individual Requests
1. Open your request
2. Go to **Headers** tab
3. Find `Postman-Token` header
4. **Uncheck** or **delete** it
5. Save the request

---

## Postman Environment Variables

Create a Postman environment with these variables:

### Variables:
```
baseUrl: http://localhost:14361/auth-service
encryptedData: (will be set from Step 1 response)
encryptedKey: (will be set from Step 1 response)
iv: (will be set from Step 1 response)
tokenEncryptedData: (will be set from Step 2 response)
tokenEncryptedKey: (will be set from Step 2 response)
tokenIv: (will be set from Step 2 response)
```

---

## Postman Request Collection

### Request 1: Encrypt
- **Method:** POST
- **URL:** `{{baseUrl}}/encrypt`
- **Headers:**
  - `Content-Type: application/json`
- **Body (raw JSON):**
```json
{
  "userId": "test-user-123"
}
```
- **Tests (to save variables):**
```javascript
var jsonData = pm.response.json();
pm.environment.set("encryptedData", jsonData.encryptedData);
pm.environment.set("encryptedKey", jsonData.encryptedKey);
pm.environment.set("iv", jsonData.iv);
```

---

### Request 2: Generate Token
- **Method:** POST
- **URL:** `{{baseUrl}}/api/v1/generateToken`
- **Headers:**
  - `Content-Type: application/json`
  - **⚠️ Make sure `Postman-Token` is NOT present!**
- **Body (raw JSON):**
```json
{
  "encryptedData": "{{encryptedData}}",
  "encryptedKey": "{{encryptedKey}}",
  "iv": "{{iv}}"
}
```
- **Tests (to save variables):**
```javascript
var jsonData = pm.response.json();
if (jsonData.data) {
    pm.environment.set("tokenEncryptedData", jsonData.data.encryptedData);
    pm.environment.set("tokenEncryptedKey", jsonData.data.encryptedKey);
    pm.environment.set("tokenIv", jsonData.data.iv);
}
```

---

### Request 3: Decrypt Token
- **Method:** POST
- **URL:** `{{baseUrl}}/decrypt`
- **Headers:**
  - `Content-Type: application/json`
- **Body (raw JSON):**
```json
{
  "encryptedData": "{{tokenEncryptedData}}",
  "encryptedKey": "{{tokenEncryptedKey}}",
  "iv": "{{tokenIv}}"
}
```

---

## Troubleshooting

### Error: "Unrecognized field encryptedData"
**Cause:** Postman-Token header is present, causing decryption to be skipped.

**Fix:** Remove the `Postman-Token` header from the request.

### Error: "Missing mandatory header: userId"
**Cause:** This is expected for `/encrypt` and `/decrypt` endpoints. They don't require authentication.

**Fix:** This error should not occur for encryption/decryption endpoints. If it does, check that `jwt.security.enabled=false` in `application.properties`.

### Error: "statusCode: 103"
**Cause:** JWT filter is intercepting the request.

**Fix:** Ensure `jwt.security.enabled=false` in `application.properties` and restart the service.

---

## Quick Reference

| Endpoint | Requires Auth | Requires Encryption | Postman-Token Header |
|----------|--------------|---------------------|---------------------|
| `/encrypt` | ❌ No | ❌ No (plain JSON) | ✅ OK |
| `/api/v1/generateToken` | ❌ No | ✅ Yes (encrypted) | ❌ **Remove it!** |
| `/decrypt` | ❌ No | ❌ No (encrypted wrapper) | ✅ OK |

---

## Testing Checklist

- [ ] `jwt.security.enabled=false` in `application.properties`
- [ ] `encryption.enabled=true` in `application.properties`
- [ ] `Postman-Token` header removed from `/generateToken` request
- [ ] Service restarted after configuration changes
- [ ] All three steps (encrypt → generate → decrypt) completed successfully

---

## Expected Flow

1. **Encrypt:** Send `{"userId": "test-user-123"}` → Get encrypted wrapper
2. **Generate Token:** Send encrypted wrapper → Get encrypted token response
3. **Decrypt:** Send encrypted token response → Get `{"accessToken": "..."}`

---

**Remember:** The key is to remove the `Postman-Token` header when calling `/api/v1/generateToken` with encrypted data!


















