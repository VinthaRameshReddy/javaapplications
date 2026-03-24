# JWT Integration - Quick Start Guide

## ✅ What's Been Completed

### Phase 1: Common Objects ✅
- ✅ Fixed and enabled `JwtRequestFilter`
- ✅ Created `JwtSecurityProperties` for configuration
- ✅ Made `JwtWebSecurityConfig` optional (disabled by default)
- ✅ Added configurable bypass URLs

### Phase 2: membership-service ✅
- ✅ Added Spring Security dependency
- ✅ Created `SecurityConfig` with JWT only
- ✅ Updated `application.properties` with JWT configuration
- ✅ Service builds successfully

## 🚀 Starting the Services

### 1. Start membership-service

```bash
cd medgo2.0-api/services/membership-service
./gradlew bootRun
```

**Service will start on:** `http://localhost:11255`
**Context path:** `/member-service`

### 2. Start auth-service (for token generation)

```bash
cd medgo2.0-api/services/auth-service
./gradlew bootRun
```

**Service will start on:** `http://localhost:14361`
**Context path:** `/auth-service`

## 🔑 Generating JWT Tokens

### Method 1: Using Encryption Endpoints (Production Method)

```bash
# Step 1: Encrypt the request
curl -X POST http://localhost:14361/auth-service/encrypt \
  -H "Content-Type: application/json" \
  -d '{"userId": "test-user-123"}'

# Step 2: Use encrypted payload to generate token
curl -X POST http://localhost:14361/auth-service/api/v1/generateToken \
  -H "Content-Type: application/json" \
  -d '{
    "encryptedData": "<from-step-1>",
    "encryptedKey": "<from-step-1>",
    "iv": "<from-step-1>"
  }'

# Step 3: Decrypt the response to get token
curl -X POST http://localhost:14361/auth-service/decrypt \
  -H "Content-Type: application/json" \
  -d '{
    "encryptedData": "<from-step-2>",
    "encryptedKey": "<from-step-2>",
    "iv": "<from-step-2>"
  }'
```

### Method 2: Direct Call (If Encryption is Disabled)

If `enable.spring.security=false` in auth-service, you might be able to call directly:

```bash
curl -X POST http://localhost:14361/auth-service/api/v1/generateToken \
  -H "Content-Type: application/json" \
  -d '{"userId": "test-user-123"}'
```

## 🧪 Testing membership-service

### Test 1: Health Check (No JWT Required) ✅

```bash
curl http://localhost:11255/member-service/health
```

**Expected:** `200 OK` - Should work without JWT

### Test 2: Actuator Health (No JWT Required) ✅

```bash
curl http://localhost:11255/member-service/actuator/health
```

**Expected:** `200 OK` - Should work without JWT

### Test 3: Protected Endpoint Without JWT ❌

```bash
curl http://localhost:11255/member-service/api/v1/endpoint
```

**Expected:** `401 Unauthorized` - Encrypted error response

### Test 4: Protected Endpoint With Valid JWT ✅

```bash
curl -X GET http://localhost:11255/member-service/api/v1/endpoint \
  -H "Authorization: Bearer YOUR_JWT_TOKEN_HERE" \
  -H "userId: test-user-123"
```

**Expected:** `200 OK` - With response data and `X-Access-Token` header

### Test 5: Invalid Token ❌

```bash
curl -X GET http://localhost:11255/member-service/api/v1/endpoint \
  -H "Authorization: Bearer invalid-token" \
  -H "userId: test-user-123"
```

**Expected:** `401 Unauthorized` - "Invalid Token"

### Test 6: Missing userId Header ❌

```bash
curl -X GET http://localhost:11255/member-service/api/v1/endpoint \
  -H "Authorization: Bearer YOUR_JWT_TOKEN_HERE"
```

**Expected:** `401 Unauthorized` - "Missing mandatory header: userId"

## 📋 Configuration

### membership-service application.properties

```properties
# JWT Security Configuration
jwt.security.enabled=true
jwt.security.bypass-urls[0]=/health
jwt.security.bypass-urls[1]=/actuator/health
jwt.security.bypass-urls[2]=/actuator/**
```

### JWT Filter Behavior

- **Validates** `Authorization: Bearer <token>` header
- **Validates** `userId` header matches token subject
- **Returns** refreshed token in `X-Access-Token` header
- **Bypasses** URLs configured in `jwt.security.bypass-urls`
- **Errors** are returned as encrypted responses

## 🔍 Verification Checklist

After starting membership-service, verify:

- [ ] Service starts without errors
- [ ] Health endpoint accessible: `GET /member-service/health`
- [ ] Actuator accessible: `GET /member-service/actuator/health`
- [ ] Protected endpoints require JWT: `GET /member-service/api/v1/*`
- [ ] Valid JWT allows access
- [ ] Invalid JWT returns 401
- [ ] Missing userId header returns 401
- [ ] Token refresh works (check `X-Access-Token` header)

## 📝 Notes

1. **Token Expiration**: 10 minutes (600000ms) by default
2. **Token Refresh**: New token returned in `X-Access-Token` header on each valid request
3. **Encryption**: Error responses are encrypted
4. **Context Path**: All URLs must include `/member-service` prefix
5. **Secret Key**: Currently hardcoded in `JwtTokenUtil` (should be moved to Key Vault)

## 🐛 Troubleshooting

### Service Won't Start
- Check if port 11255 is already in use
- Verify all dependencies are available
- Check logs for errors

### JWT Validation Fails
- Verify token was generated with correct secret key
- Check token expiration (10 minutes)
- Ensure userId header matches token subject
- Verify JWT secret key matches between auth-service and membership-service

### Encryption Errors
- Verify encryption keys are configured
- Check `/encrypt` and `/decrypt` endpoints are accessible
- Verify request/response format

## 📚 Additional Documentation

- **Detailed Testing Guide**: `docs/JWT_Testing_Guide.md`
- **Integration Plan**: `docs/JWT_Integration_Plan.md`
- **Service Categorization**: `docs/JWT_Integration_Service_Categorization.md`
- **Integration Status**: `docs/JWT_Integration_Status.md`

---

**Ready to test!** Start the services and follow the testing steps above.


















