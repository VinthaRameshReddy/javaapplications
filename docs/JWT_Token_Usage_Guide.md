# JWT Token Usage Guide

## ✅ Success! Token Generated

You've successfully generated a JWT token! Here's what you received:

```json
{
    "statusCode": "000",
    "response": "SUCCESS",
    "data": {
        "accessToken": "eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJ0ZXN0LXVzZXItMTIzIiwiaWF0IjoxNzYyNzYzNzI3LCJleHAiOjE3NjI3NjQzMjd9.DfxgRH32jqEOtIb2uCVGjzlT-gy8Zggyt8JgqWq81E-q4iOjch2_R3pnd97bicIfU6V5gnP1zWvfcypcn8UvlA"
    }
}
```

## 🔍 Token Details

Your JWT token contains:
- **Subject (userId):** `test-user-123`
- **Issued At:** `1762763727` (Unix timestamp)
- **Expires At:** `1762764327` (Unix timestamp)
- **Expiration:** Token expires in **10 minutes** (600 seconds)

## 🚀 Using the Token

### Headers for Protected Endpoints

When calling protected endpoints (like membership-service), include these headers:

```
Authorization: Bearer eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJ0ZXN0LXVzZXItMTIzIiwiaWF0IjoxNzYyNzYzNzI3LCJleHAiOjE3NjI3NjQzMjd9.DfxgRH32jqEOtIb2uCVGjzlT-gy8Zggyt8JgqWq81E-q4iOjch2_R3pnd97bicIfU6V5gnP1zWvfcypcn8UvlA
userId: test-user-123
Content-Type: application/json
```

**⚠️ IMPORTANT:**
- The `userId` header **must match** the subject (`sub`) in the JWT token
- The token expires after 10 minutes - you'll need to generate a new one

---

## 📋 Example: Call Protected Endpoint

### Using Postman

**Request:**
```
GET http://localhost:PORT/member-service/api/v1/endpoint
```

**Headers:**
```
Authorization: Bearer YOUR_JWT_TOKEN_HERE
userId: test-user-123
Content-Type: application/json
```

### Using curl

```bash
curl -X GET http://localhost:PORT/member-service/api/v1/endpoint \
  -H "Authorization: Bearer eyJhbGciOiJIUzUxMiJ9..." \
  -H "userId: test-user-123" \
  -H "Content-Type: application/json"
```

---

## 🔄 Token Refresh

The JWT filter automatically refreshes tokens. When you make a request with a valid token, you'll receive a new token in the response header:

```
X-Access-Token: <new-refreshed-token>
```

**How it works:**
1. You send a request with a valid JWT token
2. The JWT filter validates the token
3. If valid, it generates a new token with extended expiration
4. The new token is returned in the `X-Access-Token` response header
5. Use the new token for subsequent requests

---

## ⏱️ Token Expiration

### Current Configuration
- **Token Validity:** 10 minutes (600 seconds)
- **Config:** `jwt.token.validity=3600000` in `application.properties` (1 hour)
- **Note:** The actual token expiration is calculated from the `jwt.token.validity` setting

### When Token Expires
- Status Code: `419` (Token Expired)
- Response: `{"statusCode": "104", "response": "invalid", "message": "Token has expired."}`
- Action: Generate a new token using the 3-step process

---

## 🧪 Testing Token with Membership Service

Now that you have a token, test it with the membership-service:

### Step 1: Ensure Membership Service is Running
```bash
cd medgo2.0-api/services/membership-service
./gradlew bootRun
```

### Step 2: Call a Protected Endpoint

**Example Request:**
```
GET http://localhost:PORT/member-service/api/v1/members
```

**Headers:**
```
Authorization: Bearer YOUR_JWT_TOKEN_HERE
userId: test-user-123
Content-Type: application/json
```

**Expected Response:**
- If token is valid: Success response with data
- If token is expired: `419` status with error message
- If token is invalid: `401` status with error message

---

## 🔐 Token Validation Flow

When you make a request with a JWT token:

1. **JWT Filter** intercepts the request
2. **Checks for Authorization header** (must start with "Bearer ")
3. **Checks for userId header** (must match token subject)
4. **Validates token signature** (using JWT secret)
5. **Checks token expiration** (must not be expired)
6. **Validates userId match** (header userId must equal token subject)
7. **Generates refreshed token** (if validation passes)
8. **Sets authentication** in SecurityContext
9. **Continues to controller** (if all validations pass)

---

## 📝 Token Generation Checklist

- [x] ✅ Service is running
- [x] ✅ Encryption endpoint works (`/encrypt`)
- [x] ✅ Token generation works (`/api/v1/generateToken`)
- [x] ✅ Decryption endpoint works (`/decrypt`)
- [x] ✅ JWT token received successfully
- [ ] ⏳ Test token with protected endpoint
- [ ] ⏳ Verify token refresh works
- [ ] ⏳ Test token expiration

---

## 🎯 Next Steps

1. **Test Token with Membership Service**
   - Start membership-service
   - Call a protected endpoint with your token
   - Verify the request is authenticated

2. **Test Token Refresh**
   - Make a request with your token
   - Check the `X-Access-Token` header in the response
   - Use the new token for subsequent requests

3. **Test Token Expiration**
   - Wait for token to expire (or modify expiration time)
   - Make a request with expired token
   - Verify you get a `419` error

4. **Integrate Other Services**
   - Continue JWT integration for other user-facing services
   - Test each service with JWT tokens
   - Verify end-to-end authentication flow

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

### Error: "Missing or invalid Authorization header"
**Cause:** Authorization header is missing or doesn't start with "Bearer ".

**Fix:** Ensure header format is: `Authorization: Bearer <token>`

---

## 📚 Related Documentation

- `docs/Auth_Service_Quick_Start.md` - Quick start guide
- `docs/Postman_JWT_Token_Generation_Fix.md` - Troubleshooting guide
- `docs/JWT_Integration_Plan.md` - Integration overview
- `docs/JWT_Testing_Guide.md` - Testing guide

---

**🎉 Congratulations! Your JWT token generation is working correctly!**

Now you can use this token to authenticate requests to protected endpoints.


















