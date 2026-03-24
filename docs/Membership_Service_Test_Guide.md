# Membership Service JWT Test Guide

## ✅ Service Configuration

- **Port:** `11255`
- **Context Path:** `/member-service`
- **JWT Security:** Enabled
- **Bypass URLs:** `/health`, `/actuator/health`, `/actuator/**`

## 🚀 Testing JWT with Membership Service

### Step 1: Generate JWT Token (from auth-service)

First, generate a JWT token using the auth-service:

**Endpoint:** `POST http://localhost:14361/auth-service/api/v1/generateToken`

**Request Body (encrypted):**
```json
{
    "userId": "test-user-123"
}
```

**Response:**
```json
{
    "statusCode": "000",
    "response": "SUCCESS",
    "data": {
        "accessToken": "eyJhbGciOiJIUzUxMiJ9..."
    }
}
```

### Step 2: Call Membership Service Endpoint

Use the JWT token to call protected endpoints in membership-service.

**Example: Get Member Profile**

**Endpoint:** `GET http://localhost:11255/member-service/api/v1/membership/memberProfile`

**Headers:**
```
Authorization: Bearer YOUR_JWT_TOKEN_HERE
userId: test-user-123
Content-Type: application/json
```

**Query Parameters:**
```
userCode: YOUR_USER_CODE
userDependentId: (optional)
```

**Expected Response:**
- If token is valid: Success response with member data (encrypted)
- If token is invalid: `401` with error message
- If token is expired: `419` with error message

---

## 📋 Available Endpoints

### 1. Get Member Profile
```
GET /api/v1/membership/memberProfile
Headers: Authorization: Bearer <token>, userId: <userId>
Query: userCode, userDependentId (optional)
```

### 2. Get Dependents
```
GET /api/v1/membership/dependent
Headers: Authorization: Bearer <token>, userId: <userId>
Query: userCode
```

### 3. Get Maternity Benefits
```
GET /api/v1/membership/maternity
Headers: Authorization: Bearer <token>, userId: <userId>
Query: userCode
```

### 4. Get Utilization Data
```
POST /api/v1/utilization/optimizedspcall
Headers: Authorization: Bearer <token>, userId: <userId>
Body: (encrypted UtilizationRequest)
```

---

## 🔐 Token Validation Flow

1. **Request comes in** with `Authorization: Bearer <token>` and `userId` header
2. **JWT Filter** intercepts the request
3. **Validates token signature** (using shared secret key)
4. **Checks token expiration**
5. **Validates userId match** (header userId must equal token subject)
6. **Generates refreshed token** (if validation passes)
7. **Sets authentication** in SecurityContext
8. **Continues to controller** (if all validations pass)

---

## ⚠️ Important Notes

1. **Token Store Fix:** The `validateToken` method has been updated to not require tokens to be in the tokenStore. It now validates based on:
   - Token signature (parsing validates this)
   - Token expiration
   - userId match

2. **Shared Secret Key:** All services use the same hardcoded secret key in `JwtTokenUtil`, so tokens generated in auth-service can be validated in membership-service.

3. **Token Refresh:** When a valid token is used, the service automatically generates a new token and returns it in the `X-Access-Token` response header.

4. **Bypass URLs:** The following URLs bypass JWT validation:
   - `/health`
   - `/actuator/health`
   - `/actuator/**`

---

## 🧪 Testing with Postman

### Setup

1. **Create Environment Variables:**
   - `auth_service_url`: `http://localhost:14361/auth-service`
   - `member_service_url`: `http://localhost:11255/member-service`
   - `jwt_token`: (will be set after token generation)
   - `user_id`: `test-user-123`

2. **Generate Token:**
   - Call `/encrypt` endpoint with `{"userId": "test-user-123"}`
   - Call `/api/v1/generateToken` with encrypted payload
   - Call `/decrypt` to get the token
   - Save token to `jwt_token` variable

3. **Call Membership Service:**
   - Set headers: `Authorization: Bearer {{jwt_token}}`, `userId: {{user_id}}`
   - Call any protected endpoint
   - Check response for success or error

---

## 🐛 Troubleshooting

### Error: "Missing mandatory header: userId"
**Cause:** The `userId` header is missing from the request.

**Fix:** Add `userId: test-user-123` header to your request (must match token subject).

### Error: "Invalid Token"
**Cause:** Token signature is invalid or userId doesn't match.

**Fix:** 
- Verify the token is correct (not corrupted)
- Ensure `userId` header matches the token subject
- Generate a new token if needed

### Error: "Token has expired"
**Cause:** Token has passed its expiration time.

**Fix:** Generate a new token using the 3-step process.

### Error: Service not responding
**Cause:** Service is still starting or not running.

**Fix:** 
- Wait for service to fully start (check logs)
- Verify service is running on port 11255
- Check if service can connect to external dependencies (Feign clients)

---

## 📚 Related Documentation

- `docs/JWT_Token_Usage_Guide.md` - JWT token usage guide
- `docs/Auth_Service_Quick_Start.md` - Auth service quick start
- `docs/JWT_Integration_Plan.md` - JWT integration overview

---

**🎉 Ready to test! Generate a token and call the membership service endpoints.**


















