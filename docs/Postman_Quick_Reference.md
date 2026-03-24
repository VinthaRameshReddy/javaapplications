# Postman Quick Reference - JWT Token Generation

## Quick Copy-Paste Request Bodies

### Step 1: Encrypt Request Payload

**POST** `https://your-server-url/auth-service/encrypt`

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

---

### Step 2: Generate Token

**POST** `https://your-server-url/auth-service/api/v1/generateToken`

**Headers:**
```
Content-Type: application/json
```

**Body:** (Replace values from Step 1 response)
```json
{
  "encryptedData": "paste-encryptedData-from-step-1",
  "encryptedKey": "paste-encryptedKey-from-step-1",
  "iv": "paste-iv-from-step-1"
}
```

---

### Step 3: Decrypt Response

**POST** `https://your-server-url/auth-service/decrypt`

**Headers:**
```
Content-Type: application/json
```

**Body:** (Replace values from Step 2 response.data)
```json
{
  "encryptedData": "paste-encryptedData-from-step-2-data",
  "encryptedKey": "paste-encryptedKey-from-step-2-data",
  "iv": "paste-iv-from-step-2-data"
}
```

**Response:** Contains `accessToken` - this is your JWT token!

---

## Example Flow

1. **Call Step 1** → Get encrypted payload
2. **Call Step 2** with encrypted payload → Get encrypted token response
3. **Call Step 3** with encrypted response → Get actual JWT token
4. **Use token** in Authorization header: `Bearer <token>`

---

## Using Token in Requests

**Headers:**
```
Authorization: Bearer <your-jwt-token>
userId: test-user-123
Content-Type: application/json
```

**Example Request:**
```
GET https://your-server-url/member-service/api/v1/endpoint
Headers:
  Authorization: Bearer eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJ0ZXN0LXVzZXItMTIzIi...
  userId: test-user-123
```

---

## Important Notes

- Replace `your-server-url` with actual server URL
- `userId` in Step 1 must match `userId` header when using the token
- Token expires after 10 minutes
- All requests/responses are encrypted (except health endpoints)

---

**For detailed setup, see:** `Postman_JWT_Token_Generation.md`


















