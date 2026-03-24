# Auth Service Startup Fix

## Issue Fixed ✅

**Problem:** Missing `PasswordEncoder` bean
**Error:** `Parameter 2 of constructor in com.medgo.auth.serviceImpl.AuthServiceImpl required a bean of type 'org.springframework.security.crypto.password.PasswordEncoder' that could not be found.`

**Solution:** Uncommented and enabled `PasswordEncoder` bean in `SecurityConfig.java`

## Files Modified

### `services/auth-service/src/main/java/com/medgo/auth/config/SecurityConfig.java`

**Before:**
```java
//    @Bean
//    public PasswordEncoder passwordEncoder() {
//        return new BCryptPasswordEncoder(10);
//    }
```

**After:**
```java
@Bean
public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder(10);
}
```

## Potential Issues

The auth-service requires:

1. **SQL Server Database Connections:**
   - `spring.datasource.medigo.jdbc-url` - Medigo database
   - `spring.datasource.membership.jdbc-url` - Membership database
   - If databases are not accessible, the service may fail to start

2. **Redis Connection:**
   - `spring.data.redis.host` - Redis server
   - If Redis is not accessible, the service may fail to start

3. **Spring Security:**
   - Currently: `enable.spring.security=false`
   - This might conflict with Spring Security dependencies

## Starting the Service

### Option 1: Start with Databases Available
```bash
cd medgo2.0-api/services/auth-service
./gradlew bootRun
```

### Option 2: Check if Service is Running
```powershell
# Check if port is listening
netstat -an | findstr "14361"

# Check health endpoint
Invoke-WebRequest -Uri "http://localhost:14361/auth-service/health" -UseBasicParsing
```

### Option 3: Check Logs
Check the terminal output or log files in:
- `medgo2.0-api/services/auth-service/logs/`

## Service Configuration

- **Port:** `14361`
- **Context Path:** `/auth-service`
- **Health Endpoint:** `http://localhost:14361/auth-service/health`
- **Token Generation:** `http://localhost:14361/auth-service/api/v1/generateToken`

## Testing Token Generation

Once the service is running, you can test token generation:

### Step 1: Encrypt Request
```bash
POST http://localhost:14361/auth-service/encrypt
Body: {"userId": "test-user-123"}
```

### Step 2: Generate Token
```bash
POST http://localhost:14361/auth-service/api/v1/generateToken
Body: {
  "encryptedData": "<from-step-1>",
  "encryptedKey": "<from-step-1>",
  "iv": "<from-step-1>"
}
```

### Step 3: Decrypt Response
```bash
POST http://localhost:14361/auth-service/decrypt
Body: {
  "encryptedData": "<from-step-2-data>",
  "encryptedKey": "<from-step-2-data>",
  "iv": "<from-step-2-data>"
}
```

## Next Steps

1. **Verify Service Starts:** Check if the service starts successfully with the PasswordEncoder fix
2. **Check Database Connections:** Ensure SQL Server databases are accessible
3. **Check Redis Connection:** Ensure Redis server is accessible
4. **Test Token Generation:** Once running, test the token generation endpoints

---

**Status:** PasswordEncoder issue fixed ✅  
**Action Required:** Verify service starts and check for database/Redis connection issues


















