# JWT Token Validation Fix

## 🔧 Problem

The `validateToken` method in `JwtTokenUtil` was checking if the token exists in a per-service `tokenStore` (ConcurrentHashMap). This meant that:

1. Tokens generated in `auth-service` were stored in `auth-service`'s tokenStore
2. When `membership-service` tried to validate the token, it checked its own tokenStore
3. Since the token wasn't in `membership-service`'s tokenStore, validation failed
4. This prevented tokens from being shared across services

## ✅ Solution

Modified the `validateToken` method to validate tokens based on:

1. **Token Signature** - Validated by parsing the token (uses shared secret key)
2. **Token Expiration** - Checks if token has expired
3. **UserId Match** - Validates that the token's subject matches the userDetails username

The tokenStore is now used for **tracking** (stores tokens after validation) but is **not required** for validation.

## 📝 Changes Made

### File: `shared/common-objects/src/main/java/com/medgo/jwt/JwtTokenUtil.java`

**Before:**
```java
public boolean validateToken(String token, UserDetails userDetails) {
    String storedUser = tokenStore.get(token);
    return storedUser != null &&
            storedUser.equals(userDetails.getUsername()) &&
            !isTokenExpired(token);
}
```

**After:**
```java
public boolean validateToken(String token, UserDetails userDetails) {
    // First check if token can be parsed (validates signature)
    // This will throw an exception if signature is invalid, which is caught in the filter
    try {
        String tokenUserId = getUsernameFromToken(token);
        
        // Validate userId matches
        if (!tokenUserId.equals(userDetails.getUsername())) {
            log.warn("Token userId '{}' does not match userDetails username '{}'", tokenUserId, userDetails.getUsername());
            return false;
        }
        
        // Check if token is expired
        if (isTokenExpired(token)) {
            log.warn("Token is expired for user: {}", tokenUserId);
            return false;
        }
        
        // Token is valid (signature valid, not expired, userId matches)
        // Optionally store in tokenStore for tracking (but not required for validation)
        tokenStore.put(token, tokenUserId);
        
        return true;
    } catch (Exception e) {
        log.warn("Token validation failed: {}", e.getMessage());
        return false;
    }
}
```

## 🎯 Benefits

1. **Cross-Service Token Validation** - Tokens generated in one service can be validated in another service
2. **Shared Secret Key** - All services use the same secret key (hardcoded in `JwtTokenUtil`), enabling token validation across services
3. **Standard JWT Validation** - Validation is now based on standard JWT properties (signature, expiration, claims) rather than in-memory storage
4. **Backward Compatible** - TokenStore is still used for tracking, so existing functionality is preserved

## 🔐 How It Works

1. **Token Generation (auth-service):**
   - User requests token with `userId`
   - `auth-service` generates JWT token using shared secret key
   - Token is signed with HS512 algorithm
   - Token contains: `sub` (userId), `iat` (issued at), `exp` (expiration)

2. **Token Validation (membership-service):**
   - Request comes in with `Authorization: Bearer <token>` and `userId` header
   - `JwtRequestFilter` intercepts the request
   - Calls `getUsernameFromToken(token)` which parses the token and validates signature
   - Calls `validateToken(token, userDetails)` which:
     - Extracts userId from token
     - Validates userId matches userDetails username
     - Checks if token is expired
     - Stores token in tokenStore for tracking
   - If validation passes, sets authentication in SecurityContext
   - Continues to controller

## ⚠️ Important Notes

1. **Shared Secret Key:** All services must use the same secret key. Currently, it's hardcoded in `JwtTokenUtil`:
   ```java
   String SECRET_KEY = "l5p2S2aXzXmBf4dhPbD/xIbb2NZz+Do5rS3T12STIXWKzVGInGdb+jWTLhfgcG5t+bdCD4gq7d0nMZtkIhY+YQ==";
   ```

2. **Token Expiration:** Tokens expire after 10 minutes (600 seconds) by default, as defined in `JWTConstants.JWT_TOKEN_VALIDITY`.

3. **Token Refresh:** When a valid token is used, the service automatically generates a new token and returns it in the `X-Access-Token` response header.

4. **Error Handling:** If token validation fails, the filter returns an encrypted error response with status code `401` (Invalid Token) or `419` (Token Expired).

## 🧪 Testing

1. **Generate Token:**
   ```bash
   POST http://localhost:14361/auth-service/api/v1/generateToken
   Headers: Content-Type: application/json
   Body: {"userId": "test-user-123"} (encrypted)
   ```

2. **Use Token in Membership Service:**
   ```bash
   GET http://localhost:11255/member-service/api/v1/membership/memberProfile
   Headers: 
     Authorization: Bearer <token>
     userId: test-user-123
   ```

3. **Verify:**
   - Token should be validated successfully
   - Request should reach the controller
   - Response should include new token in `X-Access-Token` header

## 📚 Related Documentation

- `docs/JWT_Token_Usage_Guide.md` - JWT token usage guide
- `docs/Membership_Service_Test_Guide.md` - Membership service testing guide
- `docs/JWT_Integration_Plan.md` - JWT integration overview

---

**✅ Fix Applied! Tokens can now be validated across services.**


















