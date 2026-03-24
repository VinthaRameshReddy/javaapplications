# Membership Service JWT Request Example

## ✅ Service Status

The membership-service is **RUNNING** and the JWT filter is working correctly!

The error you received:
```json
{
    "status": "error",
    "errorCode": "INVALID_JWT",
    "message": "Missing mandatory header: userId",
    "code": 401,
    "errors": []
}
```

This means the JWT filter is intercepting requests and validating them. You just need to include the required headers.

---

## 🔐 Required Headers for JWT Authentication

When calling protected endpoints in membership-service, you **must** include:

1. **Authorization Header**: `Authorization: Bearer <your-jwt-token>`
2. **UserId Header**: `userId: <user-id>` (must match the token's subject)

---

## 📋 Complete Request Example

### Step 1: Generate JWT Token (from auth-service)

**Request:**
```
POST http://localhost:14361/auth-service/api/v1/generateToken
Content-Type: application/json
```

**Body (plain, will be encrypted):**
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
        "accessToken": "eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJ0ZXN0LXVzZXItMTIzIiwiaWF0IjoxNzYyNzYzNzI3LCJleHAiOjE3NjI3NjQzMjd9.DfxgRH32jqEOtIb2uCVGjzlT-gy8Zggyt8JgqWq81E-q4iOjch2_R3pnd97bicIfU6V5gnP1zWvfcypcn8UvlA"
    }
}
```

### Step 2: Call Membership Service Endpoint

**Request:**
```
GET http://localhost:11255/member-service/api/v1/membership/memberProfile?userCode=YOUR_USER_CODE
```

**Headers (REQUIRED):**
```
Authorization: Bearer eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJ0ZXN0LXVzZXItMTIzIiwiaWF0IjoxNzYyNzYzNzI3LCJleHAiOjE3NjI3NjQzMjd9.DfxgRH32jqEOtIb2uCVGjzlT-gy8Zggyt8JgqWq81E-q4iOjch2_R3pnd97bicIfU6V5gnP1zWvfcypcn8UvlA
userId: test-user-123
Content-Type: application/json
```

**⚠️ IMPORTANT:**
- The `userId` header **must match** the subject (`sub`) in the JWT token
- If the token was generated with `userId: "test-user-123"`, then the header must be `userId: test-user-123`

---

## 🧪 Testing with Postman

### Setup Headers in Postman

1. **Authorization Tab:**
   - Type: `Bearer Token`
   - Token: `YOUR_JWT_TOKEN_HERE`

2. **Headers Tab (add manually):**
   - Key: `userId`
   - Value: `test-user-123` (must match token subject)

3. **Headers Tab (verify):**
   - `Authorization: Bearer <token>` (added automatically from Authorization tab)
   - `userId: test-user-123` (added manually)
   - `Content-Type: application/json`

### Example Postman Request

**URL:**
```
GET http://localhost:11255/member-service/api/v1/membership/memberProfile?userCode=YOUR_USER_CODE
```

**Headers:**
```
Authorization: Bearer eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJ0ZXN0LXVzZXItMTIzIiwiaWF0IjoxNzYyNzYzNzI3LCJleHAiOjE3NjI3NjQzMjd9.DfxgRH32jqEOtIb2uCVGjzlT-gy8Zggyt8JgqWq81E-q4iOjch2_R3pnd97bicIfU6V5gnP1zWvfcypcn8UvlA
userId: test-user-123
Content-Type: application/json
```

---

## 🧪 Testing with cURL

```bash
curl -X GET "http://localhost:11255/member-service/api/v1/membership/memberProfile?userCode=YOUR_USER_CODE" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN_HERE" \
  -H "userId: test-user-123" \
  -H "Content-Type: application/json"
```

---

## 📋 Available Endpoints

### 1. Get Member Profile
```
GET /api/v1/membership/memberProfile?userCode=<code>&userDependentId=<id>
Headers: Authorization: Bearer <token>, userId: <userId>
```

### 2. Get Dependents
```
GET /api/v1/membership/dependent?userCode=<code>
Headers: Authorization: Bearer <token>, userId: <userId>
```

### 3. Get Maternity Benefits
```
GET /api/v1/membership/maternity?userCode=<code>
Headers: Authorization: Bearer <token>, userId: <userId>
```

### 4. Get Utilization Data
```
POST /api/v1/utilization/optimizedspcall
Headers: Authorization: Bearer <token>, userId: <userId>
Body: (encrypted UtilizationRequest)
```

---

## 🔍 Token Validation Flow

When you make a request:

1. **JWT Filter** intercepts the request
2. **Checks for `userId` header** - if missing, returns error (what you saw)
3. **Checks for `Authorization` header** - must start with "Bearer "
4. **Extracts token** from Authorization header
5. **Validates token signature** (using shared secret key)
6. **Validates token expiration** (must not be expired)
7. **Validates userId match** (header userId must equal token subject)
8. **Generates refreshed token** (if validation passes)
9. **Sets authentication** in SecurityContext
10. **Continues to controller** (if all validations pass)

---

## ⚠️ Common Errors

### Error: "Missing mandatory header: userId"
**Cause:** The `userId` header is missing from the request.

**Fix:** Add `userId: test-user-123` header to your request (must match token subject).

### Error: "Missing or invalid Authorization header"
**Cause:** Authorization header is missing or doesn't start with "Bearer ".

**Fix:** Ensure header format is: `Authorization: Bearer <token>`

### Error: "Invalid Token"
**Cause:** Token signature is invalid or userId doesn't match.

**Fix:** 
- Verify the token is correct (not corrupted)
- Ensure `userId` header matches the token subject
- Generate a new token if needed

### Error: "Token has expired"
**Cause:** Token has passed its expiration time.

**Fix:** Generate a new token using the 3-step process (encrypt → generate → decrypt).

---

## ✅ Success Response

When the request is successful, you'll receive:

1. **Response Body:** Encrypted response with member data
2. **Response Header:** `X-Access-Token: <new-refreshed-token>`

The `X-Access-Token` header contains a new token that you can use for subsequent requests.

---

## 🎯 Quick Test

1. **Generate Token:**
   ```
   POST http://localhost:14361/auth-service/api/v1/generateToken
   Body: {"userId": "test-user-123"} (encrypted)
   ```

2. **Call Membership Service:**
   ```
   GET http://localhost:11255/member-service/api/v1/membership/memberProfile?userCode=TEST
   Headers:
     Authorization: Bearer <token-from-step-1>
     userId: test-user-123
   ```

3. **Verify:**
   - Request should reach the controller
   - Response should include member data (encrypted)
   - Response should include `X-Access-Token` header with new token

---

## 📚 Related Documentation

- `docs/JWT_Token_Usage_Guide.md` - JWT token usage guide
- `docs/Membership_Service_Test_Guide.md` - Membership service testing guide
- `docs/JWT_Token_Validation_Fix.md` - JWT token validation fix
- `docs/Auth_Service_Quick_Start.md` - Auth service quick start

---

**✅ The service is running correctly! Just add the `userId` header to your requests.**



