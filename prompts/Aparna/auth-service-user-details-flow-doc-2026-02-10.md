# auth-service userDetails flow

## API Documentation

### POST `/api/v1/auth/userDetails` (user-details)
Fetches the authenticated user's profile details using a JWT token and device context. This endpoint validates the device header, token, and requested user identity, then returns member or non-member details. It also refreshes the JWT and returns it in the `X-Access-Token` response header.

**Controller handler**
```java
@PostMapping("/userDetails")
@EncryptResponse
public ResponseEntity<CommonResponse> getUserDetails(
        @Valid @DecryptBody(UserDetailsRequest.class) UserDetailsRequest request
) {
    logger.info("UserDetailsRequest: {}", request.userId());
    ResponseEntity<CommonResponse> validationError = SecurityValidationUtil.validateDeviceId();
    if (validationError != null) {
        return validationError;
    }
    CommonResponse response = registrationService.getUserDetails(request);
    logger.info("UserDetailsResponse: {}", response);
    return ResponseHeaderUtil.processTokenResponse(response);
}
```

**Service logic (excerpt)**
```java
@Override
public CommonResponse getUserDetails(UserDetailsRequest request) {
    LOGGER.info("GetUserDetailsRequest: {}", request.userId());

    // Validate JWT token from request header (Authorization or X-Access-Token)
    String token = getTokenFromRequest();
    if (token == null || token.isEmpty()) {
        return CommonResponse.error(
                new ErrorResponse(HttpStatus.UNAUTHORIZED.value(),
                        "JWT token is required. Please provide token in Authorization header or X-Access-Token header"),
                HttpStatus.UNAUTHORIZED.value()
        );
    }

    String tokenUsername;
    String tokenDeviceId;
    try {
        // userId header must be present and match deviceId inside token
        String headerUserId = getUserIdFromRequest();
        if (headerUserId == null || headerUserId.isEmpty()) {
            return CommonResponse.error(
                    new ErrorResponse(HttpStatus.UNAUTHORIZED.value(), "Missing mandatory header: userId"),
                    HttpStatus.UNAUTHORIZED.value()
            );
        }

        tokenDeviceId = jwtTokenUtil.getUsernameFromToken(token);
        if (tokenDeviceId == null || tokenDeviceId.isEmpty()) {
            return CommonResponse.error(
                    new ErrorResponse(HttpStatus.UNAUTHORIZED.value(), "Invalid token: deviceId not found in token"),
                    HttpStatus.UNAUTHORIZED.value()
            );
        }

        if (!headerUserId.equals(tokenDeviceId)) {
            return CommonResponse.error(
                    new ErrorResponse(HttpStatus.UNAUTHORIZED.value(), "userId header does not match token deviceId"),
                    HttpStatus.UNAUTHORIZED.value()
            );
        }

        UserDetails userDetails = jwtUserDetailsService.loadUserByUsername(tokenDeviceId);
        if (!jwtTokenUtil.validateToken(token, userDetails)) {
            return CommonResponse.error(
                    new ErrorResponse(HttpStatus.UNAUTHORIZED.value(), "Invalid or expired JWT token"),
                    HttpStatus.UNAUTHORIZED.value()
            );
        }

        tokenUsername = jwtTokenUtil.getUsernameFromTokenClaim(token);
        if (tokenUsername == null || tokenUsername.isEmpty()) {
            return CommonResponse.error(
                    new ErrorResponse(HttpStatus.UNAUTHORIZED.value(), "Invalid token: username not found in token"),
                    HttpStatus.UNAUTHORIZED.value()
            );
        }
    } catch (Exception e) {
        return CommonResponse.error(
                new ErrorResponse(HttpStatus.UNAUTHORIZED.value(), "Invalid JWT token"),
                HttpStatus.UNAUTHORIZED.value()
        );
    }

    if (!tokenUsername.equalsIgnoreCase(request.userId())) {
        return CommonResponse.error(
                new ErrorResponse(HttpStatus.FORBIDDEN.value(), "Token does not belong to the requested user"),
                HttpStatus.FORBIDDEN.value()
        );
    }

    // Member or non-member lookup, consent revalidation, token refresh, and response construction
    // ...
}
```

#### Request
- **Method:** `POST`
- **Path:** `/api/v1/auth/userDetails`
- **Consumes/Produces:** JSON (request body is decrypted by `@DecryptBody`; responses are encrypted by `@EncryptResponse` if encryption is enabled)

#### Headers (required)
- `X-DEVICE-ID`: Device identifier (required by `HeaderInterceptor` and `SecurityValidationUtil`).
- `X-APPLICATION-ID`: Trace identifier (required by `HeaderInterceptor`).
- `Authorization: Bearer <jwt>` or `X-Access-Token: <jwt>`: JWT used for authentication.
- `userId`: Must match the **deviceId** embedded in the JWT subject. Case-insensitive header lookup is supported (`userId`, `UserId`, `USERID`, `user-id`).

#### Request body
```json
{
  "userId": "email-or-mobile"
}
```

Validation (from `UserDetailsRequest`):
- `userId` is required and must not be blank.
- `userId` max length: 100 characters.

#### Success response
`CommonResponse` with `statusCode = "200"`, `response = "SUCCESS"` and data fields based on member type. The refreshed JWT is moved into the `X-Access-Token` response header (and removed from `data`).

**Member user data**
```json
{
  "statusCode": "200",
  "response": "SUCCESS",
  "data": {
    "memberCode": "M-0001234",
    "isWhitelisted": "Y",
    "firstName": "Jane",
    "sex": 2,
    "birthDate": "1990-01-01",
    "email": "jane@example.com",
    "mobile": "09171234567",
    "isConsented": true
  },
  "traceId": "..."
}
```

**Non-member user data**
```json
{
  "statusCode": "200",
  "response": "SUCCESS",
  "data": {
    "memberCode": "",
    "nonMemberCode": "NM-0200000",
    "isWhitelisted": "N",
    "email": "jane@example.com",
    "mobile": "09171234567",
    "isConsented": false
  },
  "traceId": "..."
}
```

**Response header**
- `X-Access-Token`: refreshed JWT token for the same device and user.

#### Error responses
Errors are wrapped in `CommonResponse` with `response = "ERROR"` and `data` as `ErrorResponse`.
```json
{
  "statusCode": "401",
  "response": "ERROR",
  "data": {
    "statusCode": 401,
    "errorCode": null,
    "message": "JWT token is required. Please provide token in Authorization header or X-Access-Token header",
    "errors": []
  },
  "traceId": "..."
}
```

Common error conditions:
- **400**: Missing `X-DEVICE-ID` header (`DeviceId header is required`).
- **401**: Missing/invalid JWT, missing `userId` header, token deviceId mismatch, invalid/expired token, or missing username claim.
- **403**: Token user mismatch (`Token does not belong to the requested user`) or inactive user (`CustomStatusCode.INACTIVE_USER`).
- **404**: User not found (`CustomStatusCode.USER_NOT_FOUND`, message: `This email is not registered. Would you like to register instead?`).

#### Notes
- The endpoint path uses camelCase (`/userDetails`) in code; there is no `/user-details` mapping in `AuthController`.
- Consent may be revalidated (`consentService.revalidateUserConsentFlag`) and the latest `isConsented` value is returned.
