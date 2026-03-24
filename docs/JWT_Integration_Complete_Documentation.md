# JWT Integration - Complete Documentation

## 📋 Table of Contents

1. [Overview](#overview)
2. [Architecture](#architecture)
3. [Shared Components (common-objects)](#shared-components-common-objects)
4. [Auth Service](#auth-service)
5. [Membership Service](#membership-service)
6. [Provider Service](#provider-service)
7. [Configuration Changes](#configuration-changes)
8. [Testing Guide](#testing-guide)
9. [Service Categorization](#service-categorization)

---

## 📖 Overview

JWT (JSON Web Token) authentication has been integrated into the microservices architecture. The integration follows a centralized token generation model where:

- **Auth Service**: Generates JWT tokens
- **User-Facing Services** (Membership, Provider, etc.): Validate JWT tokens for incoming requests
- **Shared Services**: Continue to use Basic Authentication + API Key (no JWT)

### Key Principles

1. **Token Generation**: Only auth-service generates JWT tokens
2. **Token Validation**: User-facing services validate tokens using shared JWT utilities
3. **Service-to-Service Communication**: Services use Basic Auth + API Key when calling shared services
4. **Header Management**: `userId` header is required for JWT validation but is NOT sent to shared services

---

## 🏗️ Architecture

### Authentication Flow

```
┌─────────────┐
│   Client    │
│  (Postman)  │
└──────┬──────┘
       │
       │ 1. POST /api/v1/generateToken
       │    Headers: userId, X-DEVICE-ID, X-APPLICATION-ID
       │    Body: { "userId": "test-user-123" }
       ▼
┌─────────────────────────────────┐
│      Auth Service               │
│  - Validates request            │
│  - Generates JWT token          │
│  - Returns encrypted token      │
└──────┬──────────────────────────┘
       │
       │ 2. Returns: { "accessToken": "eyJ..." }
       │
       ▼
┌─────────────┐
│   Client    │
│  (Postman)  │
└──────┬──────┘
       │
       │ 3. GET /api/v1/membership/dependent
       │    Headers: Authorization: Bearer <token>
       │             userId: test-user-123
       │             X-DEVICE-ID: 4554565
       │             X-APPLICATION-ID: 44545
       ▼
┌─────────────────────────────────┐
│   Membership Service            │
│  - JwtRequestFilter validates   │
│  - Checks userId header         │
│  - Validates token signature    │
│  - Sets SecurityContext         │
└──────┬──────────────────────────┘
       │
       │ 4. Feign Call to Shared Service
       │    Headers: Authorization: Basic <credentials>
       │             Ocp-Apim-Subscription-Key: <key>
       │             X-DEVICE-ID: 4554565
       │             X-APPLICATION-ID: 44545
       │    (NO userId, NO JWT Authorization)
       ▼
┌─────────────────────────────────┐
│  Shared Membership Service      │
│  - Basic Auth validation        │
│  - API Key validation           │
│  - HeaderInterceptor validates  │
│    X-DEVICE-ID, X-APPLICATION-ID│
└─────────────────────────────────┘
```

### Service Categorization

| Service Type | Authentication | Examples |
|--------------|----------------|----------|
| **User-Facing Services** | JWT Only | membership-service, provider-service |
| **Shared Services** | Basic Auth + API Key | shared-membership-service, shared-provider-service |
| **Auth Service** | Public (for token generation) | auth-service |

---

## 📦 Shared Components (common-objects)

### Existing Classes (No Changes)

These classes were already present in `common-objects` and are used by all services:

1. **`JwtTokenUtil`** (`com.medgo.jwt.JwtTokenUtil`)
   - **Location**: `shared/common-objects/src/main/java/com/medgo/jwt/JwtTokenUtil.java`
   - **Purpose**: Utility class for JWT token operations
   - **Key Methods**:
     - `generateToken(String userId)`: Generates JWT token
     - `validateToken(String token, String userId)`: Validates token signature and expiration
     - `getUserIdFromToken(String token)`: Extracts userId from token
   - **Changes Made**: Modified `validateToken()` to validate by signature and expiration only (removed tokenStore dependency for cross-service validation)

2. **`JwtUserDetailsService`** (`com.medgo.jwt.JwtUserDetailsService`)
   - **Location**: `shared/common-objects/src/main/java/com/medgo/jwt/JwtUserDetailsService.java`
   - **Purpose**: Loads user details for JWT validation
   - **Status**: No changes required

3. **`HeaderInterceptor`** (`com.medgo.config.HeaderInterceptor`)
   - **Location**: `shared/common-objects/src/main/java/com/medgo/config/HeaderInterceptor.java`
   - **Purpose**: Validates required headers (`X-DEVICE-ID`, `X-APPLICATION-ID`) for shared services
   - **Status**: No changes required
   - **Note**: This is used by shared services, NOT by user-facing services

### Modified Classes

1. **`JwtRequestFilter`** (`com.medgo.filter.JwtRequestFilter`)
   - **Location**: `shared/common-objects/src/main/java/com/medgo/filter/JwtRequestFilter.java`
   - **Purpose**: Filters incoming requests and validates JWT tokens
   - **Changes Made**:
     - Uncommented and fixed JWT validation logic
     - Added support for configurable bypass URLs via `JwtSecurityProperties`
     - Added `userId` header validation
     - Improved error handling and response formatting
   - **Key Features**:
     - Validates `Authorization: Bearer <token>` header
     - Validates `userId` header matches token subject
     - Checks token expiration and signature
     - Sets Spring Security `SecurityContext`
     - Returns encrypted error responses

### New Classes

1. **`JwtSecurityProperties`** (`com.medgo.config.JwtSecurityProperties`)
   - **Location**: `shared/common-objects/src/main/java/com/medgo/config/JwtSecurityProperties.java`
   - **Purpose**: Configuration properties for JWT security settings
   - **Properties**:
     - `enabled`: Enable/disable JWT security (default: `true`)
     - `bypass-urls`: List of URLs to bypass JWT validation (e.g., `/health`, `/actuator/**`)
   - **Usage**: Allows services to configure JWT behavior via `application.properties`

---

## 🔐 Auth Service

### Purpose
The auth-service is responsible for generating JWT tokens. It does NOT use JWT authentication itself (it's publicly accessible for token generation).

### Modified Classes

1. **`SecurityConfig`** (`com.medgo.auth.config.SecurityConfig`)
   - **Location**: `services/auth-service/src/main/java/com/medgo/auth/config/SecurityConfig.java`
   - **Changes Made**:
     - Added `PasswordEncoder` bean
     - Added `AuthenticationManager` bean
     - Added `UserDetailsService` bean named `jwtInMemoryUserDetailsService`
     - Added `SecurityFilterChain` to permit public access to:
       - `/encrypt`
       - `/decrypt`
       - `/api/v1/generateToken`
       - `/health`
       - `/actuator/**`
     - Added custom `AuthenticationProvider` for token generation
   - **Purpose**: Configures Spring Security to allow public access to token generation endpoints

2. **`GenerateTokenController`** (`com.medgo.auth.controller.GenerateTokenController`)
   - **Location**: `services/auth-service/src/main/java/com/medgo/auth/controller/GenerateTokenController.java`
   - **Status**: No code changes (uses `@DecryptBody` and `@EncryptResponse` annotations)
   - **Note**: Requires encrypted request/response bodies

### Configuration Changes

1. **`application.properties`**
   - **Location**: `services/auth-service/src/main/resources/application.properties`
   - **Changes Made**:
     - Set `jwt.security.enabled=false` to disable JWT filter for auth-service itself
     - This prevents `JwtRequestFilter` from intercepting requests in auth-service

### Endpoints

- **POST** `/api/v1/generateToken`
  - **Request Body**: `{ "userId": "test-user-123" }` (encrypted)
  - **Response**: `{ "accessToken": "eyJ..." }` (encrypted)
  - **Headers Required**: `X-DEVICE-ID`, `X-APPLICATION-ID`
  - **Note**: Request/response bodies are encrypted/decrypted automatically

---

## 👥 Membership Service

### Purpose
User-facing service that validates JWT tokens for incoming requests and uses Basic Auth + API Key for shared service calls.

### New Classes

1. **`SecurityConfig`** (`com.medgo.member.config.SecurityConfig`)
   - **Location**: `services/membership-service/src/main/java/com/medgo/member/config/SecurityConfig.java`
   - **Purpose**: Configures Spring Security for JWT authentication
   - **Key Features**:
     - Uses `JwtRequestFilter` for JWT validation
     - Allows public access to `/health` and `/actuator/**`
     - Requires authentication for all other endpoints
     - Stateless session management

### Modified Classes

1. **`MemberServiceApplication`** (`com.medgo.member.MemberServiceApplication`)
   - **Location**: `services/membership-service/src/main/java/com/medgo/member/MemberServiceApplication.java`
   - **Changes Made**:
     - Added JWT component scanning: `"com.medgo.filter"`, `"com.medgo.jwt"`
     - This enables discovery of `JwtRequestFilter` and `JwtUserDetailsService`

2. **`FeignClientConfig`** (`com.medgo.member.config.FeignClientConfig`)
   - **Location**: `services/membership-service/src/main/java/com/medgo/member/config/FeignClientConfig.java`
   - **Changes Made**:
     - Added `removeUserIdHeaderInterceptor` (Order 1): Removes `userId` and JWT `Authorization` headers
     - Added `basicAuthRequestInterceptor` (Order 2): Adds Basic Auth credentials
     - Added `apimSubscriptionKeyInterceptor` (Order 3): Adds API Key header
     - Added `headerPropagationInterceptor` (Order 4): Propagates `X-DEVICE-ID`, `X-APPLICATION-ID`, `X-CLIENT-ID`, `X-PLATFORM`
     - Added `headerCleanupInterceptor` (Order 5): Removes `Postman-Token`, sets `Accept` header, removes `Content-Type` for GET requests
     - Added fallback to `application.properties` for credentials if Azure Key Vault fails
     - Updated Key Vault secret names to match shared service expectations
   - **Purpose**: Ensures Feign clients send correct headers to shared services (Basic Auth + API Key, NOT JWT)

### Configuration Changes

1. **`build.gradle`**
   - **Location**: `services/membership-service/build.gradle`
   - **Changes Made**:
     - Added `spring-boot-starter-security` dependency

2. **`application.properties`**
   - **Location**: `services/membership-service/src/main/resources/application.properties`
   - **Changes Made**:
     - Removed security auto-configuration exclusions
     - Added `jwt.security.enabled=true`
     - Added `jwt.security.bypass-urls` for health checks and actuator endpoints
     - Added `jwt.token-validity=600000`
     - Added Feign client credentials (Basic Auth, API Key) as fallback

### Request Flow

1. **Incoming Request**:
   - Headers: `Authorization: Bearer <token>`, `userId: test-user-123`, `X-DEVICE-ID: 4554565`, `X-APPLICATION-ID: 44545`
   - `JwtRequestFilter` validates token and `userId` header
   - Request proceeds to controller

2. **Outgoing Feign Request** (to shared service):
   - Headers: `Authorization: Basic <credentials>`, `Ocp-Apim-Subscription-Key: <key>`, `X-DEVICE-ID: 4554565`, `X-APPLICATION-ID: 44545`
   - NO `userId` header
   - NO JWT `Authorization` header

---

## 🏥 Provider Service

### Purpose
User-facing service that validates JWT tokens for incoming requests and uses Basic Auth + API Key for shared service calls.

### New Classes

1. **`SecurityConfig`** (`com.medgo.provider.config.SecurityConfig`)
   - **Location**: `services/provider-service/src/main/java/com/medgo/provider/config/SecurityConfig.java`
   - **Purpose**: Configures Spring Security for JWT authentication
   - **Key Features**:
     - Uses `JwtRequestFilter` for JWT validation
     - Allows public access to `/health` and `/actuator/**`
     - Requires authentication for all other endpoints
     - Stateless session management

### Modified Classes

1. **`ProviderGatewayApplication`** (`com.medgo.provider.ProviderGatewayApplication`)
   - **Location**: `services/provider-service/src/main/java/com/medgo/provider/ProviderGatewayApplication.java`
   - **Changes Made**:
     - Added JWT component scanning: `"com.medgo.filter"`, `"com.medgo.jwt"`
     - This enables discovery of `JwtRequestFilter` and `JwtUserDetailsService`

2. **`FeignClientConfig`** (`com.medgo.provider.config.FeignClientConfig`)
   - **Location**: `services/provider-service/src/main/java/com/medgo/provider/config/FeignClientConfig.java`
   - **Changes Made**:
     - Added `removeUserIdHeaderInterceptor` (Order 1): Removes `userId` and JWT `Authorization` headers
     - Added `@Order` annotations to all interceptors for proper execution order
     - Added `headerPropagationInterceptor` (Order 2): Adds `Postman-Token: bypass-encryption`
     - Added `basicAuthRequestInterceptor` (Order 3): Adds Basic Auth credentials
     - Added `apimSubscriptionKeyInterceptor` (Order 4): Adds API Key header
   - **Purpose**: Ensures Feign clients send correct headers to shared services (Basic Auth + API Key, NOT JWT)

### Configuration Changes

1. **`build.gradle`**
   - **Location**: `services/provider-service/build.gradle`
   - **Changes Made**:
     - Added `spring-boot-starter-security` dependency

2. **`application.properties`**
   - **Location**: `services/provider-service/src/main/resources/application.properties`
   - **Changes Made**:
     - Removed security auto-configuration exclusions
     - Added `jwt.security.enabled=true`
     - Added `jwt.security.bypass-urls` for health checks and actuator endpoints
     - Added `jwt.token-validity=600000`
     - Removed old security disable flags

### Request Flow

1. **Incoming Request**:
   - Headers: `Authorization: Bearer <token>`, `userId: test-user-123`, `X-DEVICE-ID: 4554565`, `X-APPLICATION-ID: 44545`
   - `JwtRequestFilter` validates token and `userId` header
   - Request proceeds to controller

2. **Outgoing Feign Request** (to shared service):
   - Headers: `Authorization: Basic <credentials>`, `Ocp-Apim-Subscription-Key: <key>`, `Postman-Token: bypass-encryption`
   - NO `userId` header
   - NO JWT `Authorization` header
   - **Note**: Provider-service sends `Postman-Token` header (different from membership-service)

---

## ⚙️ Configuration Changes

### Common Configuration Properties

All user-facing services use the following JWT configuration:

```properties
# JWT Security Configuration
jwt.security.enabled=true
jwt.security.bypass-urls[0]=/health
jwt.security.bypass-urls[1]=/actuator/health
jwt.security.bypass-urls[2]=/actuator/**
jwt.token-validity=600000
```

### Service-Specific Configuration

#### Auth Service
```properties
jwt.security.enabled=false  # JWT filter disabled for auth-service itself
```

#### Membership Service
```properties
# Feign client credentials (fallback if Key Vault fails)
shared.membership.basic.user=internal-client
shared.membership.basic.password=secret
shared.membership.apim.header=Ocp-Apim-Subscription-Key
shared.membership.apim.key=62670b5bf89b41038ff0284eea190b0c
```

#### Provider Service
```properties
# Credentials loaded from Azure Key Vault
# Secret names: shared-provider-basic-user, shared-provider-basic-password, etc.
```

---

## 🧪 Testing Guide

### Step 1: Generate JWT Token

**Endpoint**: `POST /api/v1/generateToken`

**Request** (Postman):
```
URL: http://localhost:14361/auth-service/api/v1/generateToken
Method: POST
Headers:
  X-DEVICE-ID: 4554565
  X-APPLICATION-ID: 44545
  Content-Type: application/json
Body (encrypted):
{
  "userId": "test-user-123"
}
```

**Response** (encrypted):
```json
{
  "statusCode": "000",
  "response": "SUCCESS",
  "data": {
    "accessToken": "eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJ0ZXN0LXVzZXItMTIzIiwiaWF0IjoxNzYyNzYzNzI3LCJleHAiOjE3NjI3NjQzMjd9.DfxgRH32jqEOtIb2uCVGjzlT-gy8Zggyt8JgqWq81E-q4iOjch2_R3pnd97bicIfU6V5gnP1zWvfcypcn8UvlA"
  },
  "traceId": "224545"
}
```

**Important Notes**:
- Remove `Postman-Token` header from Postman request (causes decryption issues)
- Request/response bodies are automatically encrypted/decrypted
- Token is valid for 10 minutes (600000 ms)

### Step 2: Use JWT Token in User-Facing Service

**Endpoint**: `GET /api/v1/membership/dependent?userCode=1158094`

**Request** (Postman):
```
URL: http://localhost:11255/member-service/api/v1/membership/dependent?userCode=1158094
Method: GET
Headers:
  Authorization: Bearer eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJ0ZXN0LXVzZXItMTIzIiwiaWF0IjoxNzYyNzYzNzI3LCJleHAiOjE3NjI3NjQzMjd9.DfxgRH32jqEOtIb2uCVGjzlT-gy8Zggyt8JgqWq81E-q4iOjch2_R3pnd97bicIfU6V5gnP1zWvfcypcn8UvlA
  userId: test-user-123
  X-DEVICE-ID: 4554565
  X-APPLICATION-ID: 44545
```

**Response**: Service response (encrypted)

**Validation**:
- `JwtRequestFilter` validates token signature
- `JwtRequestFilter` validates token expiration
- `JwtRequestFilter` validates `userId` header matches token subject
- Request proceeds to controller if validation succeeds

### Step 3: Verify Feign Client Headers

When membership-service calls shared-membership-service, verify that:
- ✅ `Authorization: Basic <credentials>` is present
- ✅ `Ocp-Apim-Subscription-Key: <key>` is present
- ✅ `X-DEVICE-ID: 4554565` is present
- ✅ `X-APPLICATION-ID: 44545` is present
- ✅ `userId` header is NOT present
- ✅ JWT `Authorization: Bearer` header is NOT present

---

## 📊 Service Categorization

### User-Facing Services (JWT Authentication)

| Service | Status | JWT Enabled | Shared Service Auth |
|---------|--------|-------------|---------------------|
| membership-service | ✅ Integrated | Yes | Basic Auth + API Key |
| provider-service | ✅ Integrated | Yes | Basic Auth + API Key |
| appointment-service | ⏳ Pending | No | N/A |
| claims-service | ⏳ Pending | No | N/A |
| communication-service | ⏳ Pending | No | N/A |
| facescan-service | ⏳ Pending | No | N/A |
| filemanagement-service | ⏳ Pending | No | N/A |
| loa-service | ⏳ Pending | No | N/A |
| reimbursement-service | ⏳ Pending | No | N/A |

### Shared Services (Basic Auth + API Key)

| Service | Status | Authentication |
|---------|--------|----------------|
| shared-membership-service | ✅ Working | Basic Auth + API Key |
| shared-provider-service | ✅ Working | Basic Auth + API Key |
| shared-notification-service | ✅ Working | Basic Auth + API Key |
| shared-reimbursement-service | ✅ Working | Basic Auth + API Key |
| shared-utilization-service | ✅ Working | Basic Auth + API Key |
| shared-virtualid-service | ✅ Working | Basic Auth + API Key |

### Auth Service (Public)

| Service | Status | Authentication |
|---------|--------|----------------|
| auth-service | ✅ Working | Public (for token generation) |

---

## 🔑 Key Headers

### Headers Required for JWT Validation

| Header | Required By | Purpose |
|--------|-------------|---------|
| `Authorization: Bearer <token>` | User-facing services | JWT token for authentication |
| `userId: <user-id>` | User-facing services | Must match token subject |
| `X-DEVICE-ID: <device-id>` | All services | Device identification |
| `X-APPLICATION-ID: <app-id>` | All services | Application/trace identification |

### Headers Required for Shared Service Calls

| Header | Required By | Purpose |
|--------|-------------|---------|
| `Authorization: Basic <credentials>` | Shared services | Basic authentication |
| `Ocp-Apim-Subscription-Key: <key>` | Shared services | API Key authentication |
| `X-DEVICE-ID: <device-id>` | Shared services | Required by HeaderInterceptor |
| `X-APPLICATION-ID: <app-id>` | Shared services | Required by HeaderInterceptor |

### Headers NOT Sent to Shared Services

| Header | Reason |
|--------|--------|
| `userId` | Shared services don't need it |
| `Authorization: Bearer <token>` | Shared services use Basic Auth, not JWT |

---

## 📝 Summary of Changes

### New Classes Created

1. **`JwtSecurityProperties`** (common-objects)
   - Configuration properties for JWT security

2. **`SecurityConfig`** (membership-service)
   - Spring Security configuration for JWT

3. **`SecurityConfig`** (provider-service)
   - Spring Security configuration for JWT

### Modified Classes

1. **`JwtRequestFilter`** (common-objects)
   - Enabled and fixed JWT validation logic

2. **`JwtTokenUtil`** (common-objects)
   - Modified `validateToken()` for cross-service validation

3. **`SecurityConfig`** (auth-service)
   - Added Spring Security beans and public access configuration

4. **`MemberServiceApplication`** (membership-service)
   - Added JWT component scanning

5. **`FeignClientConfig`** (membership-service)
   - Added header management interceptors

6. **`ProviderGatewayApplication`** (provider-service)
   - Added JWT component scanning

7. **`FeignClientConfig`** (provider-service)
   - Added header management interceptors

### Configuration Files Modified

1. **`build.gradle`** (membership-service, provider-service)
   - Added `spring-boot-starter-security` dependency

2. **`application.properties`** (auth-service, membership-service, provider-service)
   - Added JWT security configuration
   - Updated security settings

---

## 🚀 Next Steps

### For Remaining Services

To integrate JWT into other user-facing services, follow these steps:

1. **Add Spring Security Dependency**
   ```gradle
   implementation 'org.springframework.boot:spring-boot-starter-security:3.2.5'
   ```

2. **Create SecurityConfig**
   - Copy from membership-service or provider-service
   - Adjust package name

3. **Update Application Class**
   - Add JWT component scanning: `"com.medgo.filter"`, `"com.medgo.jwt"`

4. **Update FeignClientConfig**
   - Add `removeUserIdHeaderInterceptor` to remove `userId` and JWT headers
   - Ensure Basic Auth + API Key are sent to shared services

5. **Update application.properties**
   - Add JWT security configuration
   - Remove security auto-configuration exclusions

6. **Test**
   - Generate token from auth-service
   - Use token in service requests
   - Verify shared service calls use Basic Auth + API Key

---

## 📚 Related Documentation

- [JWT Integration Plan](./JWT_Integration_Plan.md)
- [JWT Testing Guide](./JWT_Testing_Guide.md)
- [Postman JWT Token Generation](./Postman_JWT_Token_Generation.md)
- [Auth Service Quick Start](./Auth_Service_Quick_Start.md)
- [JWT Token Usage Guide](./JWT_Token_Usage_Guide.md)

---

## ✅ Checklist for JWT Integration

- [x] Auth-service generates JWT tokens
- [x] Membership-service validates JWT tokens
- [x] Provider-service validates JWT tokens
- [x] Feign clients use Basic Auth + API Key for shared services
- [x] `userId` header is NOT sent to shared services
- [x] JWT `Authorization` header is NOT sent to shared services
- [x] Health checks and actuator endpoints are publicly accessible
- [x] Token validation includes signature, expiration, and userId matching
- [x] Error responses are encrypted
- [x] Configuration is externalized via `application.properties`

---

**Last Updated**: 2025-01-10
**Version**: 1.0
**Author**: JWT Integration Team



