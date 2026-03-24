# Postman Request Bodies - JWT Token Generation

## Auth Service Base URL
**Replace with your actual server URL:**
```
https://your-auth-service-url/auth-service
```

---

## Request 1: Encrypt Request Payload

### Endpoint
```
POST /auth-service/encrypt
```

### Headers
```
Content-Type: application/json
```

### Body (raw JSON)
```json
{
  "userId": "test-user-123"
}
```

### Expected Response
```json
{
  "encryptedData": "base64-encrypted-data",
  "encryptedKey": "base64-encrypted-key",
  "iv": "base64-initialization-vector"
}
```

**Copy these values for the next request!**

---

## Request 2: Generate JWT Token

### Endpoint
```
POST /auth-service/api/v1/generateToken
```

### Headers
```
Content-Type: application/json
```

### Body (raw JSON)
**Replace the values with response from Request 1:**
```json
{
  "encryptedData": "PASTE-encryptedData-FROM-REQUEST-1",
  "encryptedKey": "PASTE-encryptedKey-FROM-REQUEST-1",
  "iv": "PASTE-iv-FROM-REQUEST-1"
}
```

### Expected Response (Encrypted)
```json
{
  "statusCode": "000",
  "response": "SUCCESS",
  "data": {
    "encryptedData": "encrypted-response-data",
    "encryptedKey": "encrypted-response-key",
    "iv": "response-iv"
  },
  "traceId": "trace-id-here"
}
```

**Copy the `data` object for the next request!**

---

## Request 3: Decrypt Response to Get JWT Token

### Endpoint
```
POST /auth-service/decrypt
```

### Headers
```
Content-Type: application/json
```

### Body (raw JSON)
**Replace the values with `data` object from Request 2:**
```json
{
  "encryptedData": "PASTE-encryptedData-FROM-REQUEST-2-DATA",
  "encryptedKey": "PASTE-encryptedKey-FROM-REQUEST-2-DATA",
  "iv": "PASTE-iv-FROM-REQUEST-2-DATA"
}
```

### Expected Response
```json
{
  "accessToken": "eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJ0ZXN0LXVzZXItMTIzIiwiaWF0IjoxNjk5OTk5OTk5LCJleHAiOjE3MDAwMDU5OTl9.YOUR-ACTUAL-JWT-TOKEN-HERE"
}
```

**🎉 This `accessToken` is your JWT token!**

---

## Using the JWT Token

### Headers for Protected Endpoints
```
Authorization: Bearer eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJ0ZXN0LXVzZXItMTIzIi...
userId: test-user-123
Content-Type: application/json
```

### Example: Call Protected Endpoint
```
GET /member-service/api/v1/endpoint
Headers:
  Authorization: Bearer YOUR_JWT_TOKEN_HERE
  userId: test-user-123
```

---

## Complete Postman Collection JSON

You can import this into Postman:

```json
{
  "info": {
    "name": "JWT Token Generation",
    "schema": "https://schema.getpostman.com/json/collection/v2.1.0/collection.json"
  },
  "item": [
    {
      "name": "1. Encrypt Request",
      "request": {
        "method": "POST",
        "header": [
          {
            "key": "Content-Type",
            "value": "application/json"
          }
        ],
        "body": {
          "mode": "raw",
          "raw": "{\n  \"userId\": \"test-user-123\"\n}"
        },
        "url": {
          "raw": "{{baseUrl}}/encrypt",
          "host": ["{{baseUrl}}"],
          "path": ["encrypt"]
        }
      }
    },
    {
      "name": "2. Generate Token",
      "request": {
        "method": "POST",
        "header": [
          {
            "key": "Content-Type",
            "value": "application/json"
          }
        ],
        "body": {
          "mode": "raw",
          "raw": "{\n  \"encryptedData\": \"{{encryptedData}}\",\n  \"encryptedKey\": \"{{encryptedKey}}\",\n  \"iv\": \"{{iv}}\"\n}"
        },
        "url": {
          "raw": "{{baseUrl}}/api/v1/generateToken",
          "host": ["{{baseUrl}}"],
          "path": ["api", "v1", "generateToken"]
        }
      }
    },
    {
      "name": "3. Decrypt Token",
      "request": {
        "method": "POST",
        "header": [
          {
            "key": "Content-Type",
            "value": "application/json"
          }
        ],
        "body": {
          "mode": "raw",
          "raw": "{\n  \"encryptedData\": \"{{tokenEncryptedData}}\",\n  \"encryptedKey\": \"{{tokenEncryptedKey}}\",\n  \"iv\": \"{{tokenIv}}\"\n}"
        },
        "url": {
          "raw": "{{baseUrl}}/decrypt",
          "host": ["{{baseUrl}}"],
          "path": ["decrypt"]
        }
      }
    }
  ],
  "variable": [
    {
      "key": "baseUrl",
      "value": "https://your-auth-service-url/auth-service",
      "type": "string"
    }
  ]
}
```

---

## Quick Copy-Paste Guide

### Step 1: Encrypt
**URL:** `POST https://your-server/auth-service/encrypt`
```json
{"userId": "test-user-123"}
```

### Step 2: Generate Token  
**URL:** `POST https://your-server/auth-service/api/v1/generateToken`
```json
{
  "encryptedData": "<from-step-1>",
  "encryptedKey": "<from-step-1>",
  "iv": "<from-step-1>"
}
```

### Step 3: Decrypt
**URL:** `POST https://your-server/auth-service/decrypt`
```json
{
  "encryptedData": "<from-step-2-data.encryptedData>",
  "encryptedKey": "<from-step-2-data.encryptedKey>",
  "iv": "<from-step-2-data.iv>"
}
```

### Result: JWT Token
```json
{"accessToken": "eyJhbGciOiJIUzUxMiJ9..."}
```

---

## Notes

- Replace `your-server` with actual auth-service URL
- `userId` can be any string (e.g., "test-user-123", "user-456")
- Token expires after 10 minutes (600000ms)
- Use token in `Authorization: Bearer <token>` header
- Include `userId: <user-id>` header matching the token subject


















