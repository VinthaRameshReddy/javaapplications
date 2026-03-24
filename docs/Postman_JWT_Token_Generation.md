# Postman Collection - JWT Token Generation

## Overview
This guide provides Postman request configurations for generating JWT tokens from the deployed auth-service.

**Base URL:** `https://your-auth-service-url/auth-service` (Replace with actual deployed URL)
**Context Path:** `/auth-service`

## Step 1: Encrypt Request Payload

### Request Configuration

**Method:** `POST`  
**URL:** `{{baseUrl}}/encrypt`  
**Headers:**
```
Content-Type: application/json
```

**Body (raw JSON):**
```json
{
  "userId": "test-user-123"
}
```

**Expected Response:**
```json
{
  "encryptedData": "encrypted-data-here",
  "encryptedKey": "encrypted-key-here",
  "iv": "initialization-vector-here"
}
```

### Postman Setup
1. Create a new request named "1. Encrypt Request"
2. Set method to `POST`
3. Set URL to: `{{baseUrl}}/encrypt`
4. Go to **Headers** tab:
   - Key: `Content-Type`, Value: `application/json`
5. Go to **Body** tab:
   - Select **raw**
   - Select **JSON** from dropdown
   - Paste the JSON body above
6. Click **Send**
7. Copy the response values (`encryptedData`, `encryptedKey`, `iv`) - you'll need them for Step 2

---

## Step 2: Generate JWT Token

### Request Configuration

**Method:** `POST`  
**URL:** `{{baseUrl}}/api/v1/generateToken`  
**Headers:**
```
Content-Type: application/json
```

**Body (raw JSON):**
```json
{
  "encryptedData": "paste-encryptedData-from-step-1",
  "encryptedKey": "paste-encryptedKey-from-step-1",
  "iv": "paste-iv-from-step-1"
}
```

**Expected Response (Encrypted):**
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

### Postman Setup
1. Create a new request named "2. Generate Token"
2. Set method to `POST`
3. Set URL to: `{{baseUrl}}/api/v1/generateToken`
4. Go to **Headers** tab:
   - Key: `Content-Type`, Value: `application/json`
5. Go to **Body** tab:
   - Select **raw**
   - Select **JSON** from dropdown
   - Paste the JSON body above, replacing the values with response from Step 1
6. Click **Send**
7. Copy the `data` object from response - you'll need it for Step 3

---

## Step 3: Decrypt Response to Get Token

### Request Configuration

**Method:** `POST`  
**URL:** `{{baseUrl}}/decrypt`  
**Headers:**
```
Content-Type: application/json
```

**Body (raw JSON):**
```json
{
  "encryptedData": "paste-encryptedData-from-step-2-data",
  "encryptedKey": "paste-encryptedKey-from-step-2-data",
  "iv": "paste-iv-from-step-2-data"
}
```

**Expected Response:**
```json
{
  "accessToken": "eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJ0ZXN0LXVzZXItMTIzIiwiaWF0IjoxNjk5OTk5OTk5LCJleHAiOjE3MDAwMDU5OTl9.actual-jwt-token-here"
}
```

### Postman Setup
1. Create a new request named "3. Decrypt Response"
2. Set method to `POST`
3. Set URL to: `{{baseUrl}}/decrypt`
4. Go to **Headers** tab:
   - Key: `Content-Type`, Value: `application/json`
5. Go to **Body** tab:
   - Select **raw**
   - Select **JSON** from dropdown
   - Paste the JSON body above, replacing values with `data` object from Step 2
6. Click **Send**
7. **Copy the `accessToken` value** - this is your JWT token!

---

## Automated Postman Collection (Using Variables)

### Environment Variables Setup

Create a Postman environment with these variables:

```
baseUrl: https://your-auth-service-url
userId: test-user-123
encryptedData: (will be set automatically)
encryptedKey: (will be set automatically)
iv: (will be set automatically)
jwtToken: (will be set automatically)
```

### Request 1: Encrypt Request (with Tests)

**URL:** `{{baseUrl}}/encrypt`  
**Method:** `POST`  
**Body:**
```json
{
  "userId": "{{userId}}"
}
```

**Tests Script:**
```javascript
if (pm.response.code === 200) {
    var jsonData = pm.response.json();
    pm.environment.set("encryptedData", jsonData.encryptedData);
    pm.environment.set("encryptedKey", jsonData.encryptedKey);
    pm.environment.set("iv", jsonData.iv);
    console.log("Encryption successful");
}
```

### Request 2: Generate Token (with Tests)

**URL:** `{{baseUrl}}/api/v1/generateToken`  
**Method:** `POST`  
**Body:**
```json
{
  "encryptedData": "{{encryptedData}}",
  "encryptedKey": "{{encryptedKey}}",
  "iv": "{{iv}}"
}
```

**Tests Script:**
```javascript
if (pm.response.code === 200) {
    var jsonData = pm.response.json();
    if (jsonData.data) {
        pm.environment.set("tokenEncryptedData", jsonData.data.encryptedData);
        pm.environment.set("tokenEncryptedKey", jsonData.data.encryptedKey);
        pm.environment.set("tokenIv", jsonData.data.iv);
        console.log("Token generation successful");
    }
}
```

### Request 3: Decrypt Token (with Tests)

**URL:** `{{baseUrl}}/decrypt`  
**Method:** `POST`  
**Body:**
```json
{
  "encryptedData": "{{tokenEncryptedData}}",
  "encryptedKey": "{{tokenEncryptedKey}}",
  "iv": "{{tokenIv}}"
}
```

**Tests Script:**
```javascript
if (pm.response.code === 200) {
    var jsonData = pm.response.json();
    if (jsonData.accessToken) {
        pm.environment.set("jwtToken", jsonData.accessToken);
        console.log("JWT Token: " + jsonData.accessToken);
    }
}
```

---

## Complete Postman Collection JSON

Save this as a Postman collection:

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
      },
      "event": [
        {
          "listen": "test",
          "script": {
            "exec": [
              "if (pm.response.code === 200) {",
              "    var jsonData = pm.response.json();",
              "    pm.environment.set(\"encryptedData\", jsonData.encryptedData);",
              "    pm.environment.set(\"encryptedKey\", jsonData.encryptedKey);",
              "    pm.environment.set(\"iv\", jsonData.iv);",
              "    console.log(\"Encryption successful\");",
              "}"
            ]
          }
        }
      ]
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
      },
      "event": [
        {
          "listen": "test",
          "script": {
            "exec": [
              "if (pm.response.code === 200) {",
              "    var jsonData = pm.response.json();",
              "    if (jsonData.data) {",
              "        pm.environment.set(\"tokenEncryptedData\", jsonData.data.encryptedData);",
              "        pm.environment.set(\"tokenEncryptedKey\", jsonData.data.encryptedKey);",
              "        pm.environment.set(\"tokenIv\", jsonData.data.iv);",
              "        console.log(\"Token generation successful\");",
              "    }",
              "}"
            ]
          }
        }
      ]
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
      },
      "event": [
        {
          "listen": "test",
          "script": {
            "exec": [
              "if (pm.response.code === 200) {",
              "    var jsonData = pm.response.json();",
              "    if (jsonData.accessToken) {",
              "        pm.environment.set(\"jwtToken\", jsonData.accessToken);",
              "        console.log(\"JWT Token: \" + jsonData.accessToken);",
              "    }",
              "}"
            ]
          }
        }
      ]
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

## Quick Reference - Request Bodies

### Step 1: Encrypt
```json
{
  "userId": "test-user-123"
}
```

### Step 2: Generate Token
```json
{
  "encryptedData": "<from-step-1-response>",
  "encryptedKey": "<from-step-1-response>",
  "iv": "<from-step-1-response>"
}
```

### Step 3: Decrypt
```json
{
  "encryptedData": "<from-step-2-data.encryptedData>",
  "encryptedKey": "<from-step-2-data.encryptedKey>",
  "iv": "<from-step-2-data.iv>"
}
```

---

## Using the Token

Once you have the JWT token, use it in your requests to protected endpoints:

**Header:**
```
Authorization: Bearer <your-jwt-token>
userId: test-user-123
```

**Example:**
```
GET /member-service/api/v1/endpoint
Headers:
  Authorization: Bearer eyJhbGciOiJIUzUxMiJ9...
  userId: test-user-123
```

---

## Notes

1. **Replace Base URL**: Update `{{baseUrl}}` with your actual auth-service URL
2. **userId**: Can be any string (e.g., "test-user-123", "user-456", etc.)
3. **Token Expiration**: Tokens expire after 10 minutes (600000ms)
4. **Automated Flow**: Use Postman collection with tests to automate the 3-step process
5. **Environment Variables**: Store the token in environment variables for reuse

---

**Last Updated**: 2024


















