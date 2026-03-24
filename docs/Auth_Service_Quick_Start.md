# Auth Service - Quick Start Guide

## ✅ Service Status

**Auth Service is RUNNING!**
- **Port:** `14361`
- **Base URL:** `http://localhost:14361/auth-service`
- **Health Check:** Returns 401 (expected if security is enabled)

---

## 🚀 Generate JWT Token - 3 Simple Steps

### ⚠️ IMPORTANT: Postman Users

**If you're using Postman, you MUST remove the `Postman-Token` header from Step 2 (Generate Token) request!**

**Why?** Postman automatically adds this header, which causes decryption to be skipped and results in errors.

**How to fix:**
1. Open your request to `/api/v1/generateToken`
2. Go to **Headers** tab
3. Find and **remove/disable** the `Postman-Token` header
4. Save and send the request

See `docs/Postman_JWT_Token_Generation_Fix.md` for detailed instructions.

---

### Step 1: Encrypt Request Payload

**Endpoint:** `POST http://localhost:14361/auth-service/encrypt`

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
  "encryptedData": "base64-encrypted-data",
  "encryptedKey": "base64-encrypted-key",
  "iv": "base64-initialization-vector"
}
```

**📝 Copy these 3 values for Step 2!**

---

### Step 2: Generate JWT Token

**Endpoint:** `POST http://localhost:14361/auth-service/api/v1/generateToken`

**Headers:**
```
Content-Type: application/json
```

**⚠️ CRITICAL for Postman Users:** Make sure the `Postman-Token` header is **NOT** present! Remove it from the Headers tab.

**Body:**
```json
{
  "encryptedData": "PASTE-encryptedData-FROM-STEP-1",
  "encryptedKey": "PASTE-encryptedKey-FROM-STEP-1",
  "iv": "PASTE-iv-FROM-STEP-1"
}
```

**Response (Encrypted):**
```json
{
  "statusCode": "000",
  "response": "SUCCESS",
  "data": {
    "encryptedData": "encrypted-response-data",
    "encryptedKey": "encrypted-response-key",
    "iv": "response-iv"
  },
  "traceId": "trace-id"
}
```

**📝 Copy the `data` object for Step 3!**

---

### Step 3: Decrypt Response to Get JWT Token

**Endpoint:** `POST http://localhost:14361/auth-service/decrypt`

**Headers:**
```
Content-Type: application/json
```

**Body:**
```json
{
  "encryptedData": "PASTE-encryptedData-FROM-STEP-2-DATA",
  "encryptedKey": "PASTE-encryptedKey-FROM-STEP-2-DATA",
  "iv": "PASTE-iv-FROM-STEP-2-DATA"
}
```

**Response:**
```json
{
  "accessToken": "eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJ0ZXN0LXVzZXItMTIzIiwiaWF0IjoxNjk5OTk5OTk5LCJleHAiOjE3MDAwMDU5OTl9.YOUR-ACTUAL-JWT-TOKEN"
}
```

**🎉 SUCCESS! The `accessToken` is your JWT token!**

---

## 🔐 Using the JWT Token

### Headers for Protected Endpoints

```
Authorization: Bearer eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJ0ZXN0LXVzZXItMTIzIi...
userId: test-user-123
Content-Type: application/json
```

### Example: Call Protected Endpoint

```
GET http://localhost:PORT/member-service/api/v1/endpoint
Headers:
  Authorization: Bearer YOUR_JWT_TOKEN_HERE
  userId: test-user-123
```

---

## 📋 Postman Collection

See `docs/Postman_Request_Bodies.md` for:
- Complete Postman collection JSON
- Detailed request/response examples
- Postman environment variables setup

---

## ⚙️ Configuration

### Token Validity
- **Default:** 1 hour (3600000ms)
- **Config:** `jwt.token.validity=3600000` in `application.properties`

### JWT Secret Key
- **Config:** `jwt.secret=...` in `application.properties`
- **Note:** Should be stored in Azure Key Vault in production

---

## 🧪 Test Token Generation

### Using curl

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

## 🔍 Troubleshooting

### Service Not Starting
- Check database connections (SQL Server)
- Check Redis connection
- Check logs in `logs/` directory

### Token Generation Fails
- Verify encryption keys are configured
- Check `app.service.enckey` and `app.encryption.secret-key` in `application.properties`
- Verify `jwt.secret` is configured

### 401 Unauthorized on Health Check
- This is expected if Spring Security is enabled
- Health endpoint may require authentication
- Token generation endpoints should work without authentication

---

## 📚 Related Documentation

- `docs/Postman_Request_Bodies.md` - Complete Postman requests
- `docs/JWT_Integration_Plan.md` - JWT integration overview
- `docs/JWT_Testing_Guide.md` - Testing guide

---

## ✅ Next Steps

1. **Test Token Generation** - Use the 3-step process above
2. **Test Protected Endpoints** - Use the token in Authorization header
3. **Integrate Other Services** - Follow JWT integration plan for other services

---

**Status:** ✅ Auth Service is running and ready to generate tokens!

