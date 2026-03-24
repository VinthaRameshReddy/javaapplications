# Consent Management API Documentation

## Overview
This document describes the consent management flow and API endpoints for handling user consents in the authentication service.

## Flow Diagram

```
┌─────────────────────────────────────────────────────────────────┐
│                    CONSENT MANAGEMENT FLOW                       │
└─────────────────────────────────────────────────────────────────┘

1. USER LOGIN
   └─> Check isConsented flag from UserModel/NonMemberUserModel
       ├─> If isConsented = true: Re-validate (check for new mandatory consents)
       └─> Return isConsented in login response

2. LIST AVAILABLE CONSENTS
   POST /api/v1/auth/consents/list
   └─> Returns all enabled consents from CONSENT table
       └─> Frontend displays consents to user

3. USER ACCEPTS CONSENTS
   POST /api/v1/auth/consents/store
   ├─> Frontend sends: email + list of consent IDs (already accepted + new)
   ├─> Backend filters: Only stores NEW consent IDs (skips already stored)
   ├─> Stores new consents in USER_CONSENT table
   └─> Updates isConsented flag:
       ├─> Checks if user has accepted ALL mandatory consents
       └─> Sets isConsented = true if all mandatory accepted, else false

4. NEXT LOGIN
   └─> isConsented flag reflects current consent status
       └─> If new mandatory consent added → isConsented automatically updated to false
```

## API Endpoints

### 1. List Available Consents

**Endpoint:** `POST /api/v1/auth/consents/list`

**Description:** Returns all enabled consents from the CONSENT table.

**Request:**
```json
{}
```
(Empty request body - no parameters needed)

**Postman Request:**
```
POST http://localhost:8080/api/v1/auth/consents/list
Content-Type: application/json
Postman-Token: <any-value>  (to skip encryption for testing)

Body (raw JSON):
{}
```

**Response (Success):**
```json
{
  "statusCode": "000",
  "response": "SUCCESS",
  "data": {
    "consents": [
      {
        "id": 1,
        "type": "PRIVACY_POLICY",
        "versionNo": "1.0",
        "header": "Privacy Policy",
        "content": "We collect and use your personal information...",
        "enabled": 1,
        "required": 1,
        "createdOn": "2025-01-15T10:30:00"
      },
      {
        "id": 2,
        "type": "TERMS_OF_SERVICE",
        "versionNo": "2.0",
        "header": "Terms of Service",
        "content": "By using our service, you agree to...",
        "enabled": 1,
        "required": 1,
        "createdOn": "2025-01-20T14:20:00"
      },
      {
        "id": 3,
        "type": "MARKETING",
        "versionNo": "1.0",
        "header": "Marketing Communications",
        "content": "We would like to send you marketing emails...",
        "enabled": 1,
        "required": 0,
        "createdOn": "2025-01-25T09:15:00"
      }
    ],
    "mandatoryConsents": [
      {
        "id": 1,
        "type": "PRIVACY_POLICY",
        "versionNo": "1.0",
        "header": "Privacy Policy",
        "content": "We collect and use your personal information...",
        "enabled": 1,
        "required": 1,
        "createdOn": "2025-01-15T10:30:00"
      },
      {
        "id": 2,
        "type": "TERMS_OF_SERVICE",
        "versionNo": "2.0",
        "header": "Terms of Service",
        "content": "By using our service, you agree to...",
        "enabled": 1,
        "required": 1,
        "createdOn": "2025-01-20T14:20:00"
      }
    ],
    "optionalConsents": [
      {
        "id": 3,
        "type": "MARKETING",
        "versionNo": "1.0",
        "header": "Marketing Communications",
        "content": "We would like to send you marketing emails...",
        "enabled": 1,
        "required": 0,
        "createdOn": "2025-01-25T09:15:00"
      }
    ],
    "totalCount": 3,
    "mandatoryCount": 2,
    "optionalCount": 1
  },
  "traceId": "abc123-def456-ghi789"
}
```

**Note:** The response includes:
- `consents`: All enabled consents (mandatory + optional)
- `mandatoryConsents`: Only mandatory consents (required = 1)
- `optionalConsents`: Only optional consents (required = 0)
- `totalCount`: Total number of enabled consents
- `mandatoryCount`: Number of mandatory consents
- `optionalCount`: Number of optional consents

**Response (Error):**
```json
{
  "statusCode": "001",
  "response": "ERROR",
  "data": {
    "statusCode": 500,
    "message": "Failed to retrieve consents"
  },
  "traceId": "abc123-def456-ghi789"
}
```

---

### 2. Store User Consent

**Endpoint:** `POST /api/v1/auth/consents/store`

**Description:** Stores user consent acceptance. Only stores NEW consent IDs (skips already accepted ones).

**Request:**
```json
{
  "userId": "user@example.com",
  "consentIds": [1, 2, 3, 4, 5],
  "agreed": 1
}
```

**Field Descriptions:**
- `userId` (String, required): User's email address
- `consentIds` (List<Integer>, required): Array of consent IDs (can include already accepted + new ones)
- `agreed` (Integer, required): 1 for agreed, 0 for not agreed

**Postman Request:**
```
POST http://localhost:8080/api/v1/auth/consents/store
Content-Type: application/json
Postman-Token: <any-value>  (to skip encryption for testing)

Body (raw JSON):
{
  "userId": "user@example.com",
  "consentIds": [1, 2, 3, 4, 5],
  "agreed": 1
}
```

**Response (Success - New Consents Stored):**
```json
{
  "statusCode": "000",
  "response": "SUCCESS",
  "data": {
    "userId": "user@example.com",
    "actualUserId": 123,
    "totalConsentIdsProvided": 5,
    "alreadyStoredCount": 2,
    "newConsentIdsStored": 3,
    "skippedCount": 0,
    "storedConsentIds": [3, 4, 5],
    "message": "Stored 3 new consent(s) successfully"
  },
  "traceId": "abc123-def456-ghi789"
}
```

**Response (Success - All Already Stored):**
```json
{
  "statusCode": "000",
  "response": "SUCCESS",
  "data": {
    "userId": "user@example.com",
    "actualUserId": 123,
    "consentIds": [1, 2, 3],
    "newConsentIdsStored": 0,
    "message": "All consents are already stored"
  },
  "traceId": "abc123-def456-ghi789"
}
```

**Response (Error - User Not Found):**
```json
{
  "statusCode": "001",
  "response": "ERROR",
  "data": {
    "statusCode": 404,
    "message": "User not found"
  },
  "traceId": "abc123-def456-ghi789"
}
```

**Response (Error - Consent Not Found):**
```json
{
  "statusCode": "001",
  "response": "ERROR",
  "data": {
    "statusCode": 404,
    "message": "Consent not found"
  },
  "traceId": "abc123-def456-ghi789"
}
```

**Response (Error - Consent Not Enabled):**
```json
{
  "statusCode": "001",
  "response": "ERROR",
  "data": {
    "statusCode": 400,
    "message": "Consent is not enabled"
  },
  "traceId": "abc123-def456-ghi789"
}
```

---

## Login Response with isConsented

After implementing consent logic, all login responses now include the `isConsented` field.

### Member Login Response Example:

**Endpoint:** `POST /api/v1/auth/login/member`

**Response:**
```json
{
  "statusCode": "000",
  "response": "SUCCESS",
  "data": {
    "message": "Login Successful",
    "memberCode": "MEM-123456",
    "isWhitelisted": "N",
    "firstName": "John",
    "sex": 1,
    "birthDate": "1990-01-15",
    "email": "user@example.com",
    "mobile": "1234567890",
    "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "isConsented": false
  },
  "traceId": "abc123-def456-ghi789"
}
```

### Non-Member Login Response Example:

**Endpoint:** `POST /api/v1/auth/login/nonmember`

**Response:**
```json
{
  "statusCode": "000",
  "response": "SUCCESS",
  "data": {
    "message": "Login Successful",
    "memberCode": "",
    "nonMemberCode": "NM-0200001",
    "isWhitelisted": "N",
    "email": "nonmember@example.com",
    "mobile": "9876543210",
    "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "isConsented": true
  },
  "traceId": "abc123-def456-ghi789"
}
```

---

## How isConsented Flag Works

### Initial State:
- **New Users:** `isConsented = false` (default)
- **Migrated Users:** `isConsented = false` (default)

### When User Accepts Consents:
1. User calls `/api/v1/auth/consents/store` with consent IDs
2. System stores NEW consent IDs only (skips already stored)
3. System checks if user has accepted ALL mandatory consents (`required = 1`)
4. If ALL mandatory consents accepted → `isConsented = true`
5. If ANY mandatory consent missing → `isConsented = false`

### During Login:
1. System reads `isConsented` flag from user entity
2. If `isConsented = true`, system re-validates:
   - Checks current mandatory consents
   - Verifies user has accepted all of them
   - Updates flag if needed (handles new mandatory consents)
3. Returns `isConsented` value in login response

### When New Mandatory Consent Added:
- Users with `isConsented = true` are automatically re-validated during next login
- If they haven't accepted the new mandatory consent → `isConsented` updated to `false`
- User must accept the new mandatory consent to get `isConsented = true` again

---

## Example Scenarios

### Scenario 1: First Time User Accepting Consents

**Step 1: List Consents**
```
POST /api/v1/auth/consents/list
Response: Returns 3 consents (2 mandatory, 1 optional)
```

**Step 2: User Accepts All Consents**
```
POST /api/v1/auth/consents/store
Request: {
  "userId": "newuser@example.com",
  "consentIds": [1, 2, 3],
  "agreed": 1
}
Response: Stores all 3 consents, sets isConsented = true (all mandatory accepted)
```

**Step 3: User Logs In**
```
POST /api/v1/auth/login/member
Response: {
  ...
  "isConsented": true
}
```

### Scenario 2: User Already Has Some Consents

**Step 1: User Already Accepted Consents 1 and 2**

**Step 2: User Accepts New Consent**
```
POST /api/v1/auth/consents/store
Request: {
  "userId": "user@example.com",
  "consentIds": [1, 2, 3],  // 1,2 already stored, 3 is new
  "agreed": 1
}
Response: {
  "alreadyStoredCount": 2,
  "newConsentIdsStored": 1,
  "storedConsentIds": [3]
}
```

### Scenario 3: New Mandatory Consent Added

**Step 1: Admin adds new mandatory consent (ID: 4)**

**Step 2: User Logs In (has isConsented = true)**
```
POST /api/v1/auth/login/member
- System re-validates: User has consents [1,2,3], but mandatory consents are [1,2,4]
- User missing consent 4 → isConsented updated to false
Response: {
  ...
  "isConsented": false
}
```

**Step 3: User Accepts New Mandatory Consent**
```
POST /api/v1/auth/consents/store
Request: {
  "userId": "user@example.com",
  "consentIds": [1, 2, 3, 4],
  "agreed": 1
}
Response: Stores consent 4, sets isConsented = true (all mandatory now accepted)
```

---

## Database Tables

### CONSENT Table
- Stores available consents
- Fields: ID, TYPE, VERSION_NO, HEADER, CONTENT, ENABLED, REQUIRED
- `ENABLED = 1`: Consent is active
- `REQUIRED = 1`: Consent is mandatory

### USER_CONSENT Table
- Stores user consent acceptances
- Fields: ID, USER_ID, CONSENT_ID, AGREED
- `AGREED = 1`: User has agreed to this consent
- Links to MEDGO_USERS.ID or MEDGO_NON_MEMBER_USER.ID

### MEDGO_USERS / MEDGO_NON_MEMBER_USER Tables
- Added field: `IS_CONSENTED`
- Default: `false`
- `true`: User has accepted all mandatory consents
- `false`: User has not accepted all mandatory consents

---

## Notes

1. **Encryption:** All requests/responses are encrypted by default. Add `Postman-Token` header to skip encryption for testing.

2. **Email as UserId:** The `userId` field in store request accepts email address. System finds user in either MEMBER or NON_MEMBER table.

3. **Duplicate Prevention:** System automatically filters out already stored consent IDs, so frontend can send all consent IDs without worrying about duplicates.

4. **Automatic Re-validation:** System automatically checks consent status during login if `isConsented = true` to handle new mandatory consents.

5. **Mandatory Consents:** Only consents with `required = 1` and `enabled = 1` are considered mandatory. User must accept ALL mandatory consents for `isConsented = true`.

