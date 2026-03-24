# Security Context Flow - Visual Diagram

## Complete Request Flow with Security Context

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                              CLIENT                                         │
│  Sends HTTP Request:                                                        │
│  POST /api/v1/auth/userDetails                                             │
│  Headers:                                                                   │
│    Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...            │
│    userId: device-12345                                                     │
│    X-DEVICE-ID: device-12345                                               │
└──────────────────────────────┬──────────────────────────────────────────────┘
                                │
                                ▼
┌─────────────────────────────────────────────────────────────────────────────┐
│                    SPRING BOOT APPLICATION                                  │
│                                                                              │
│  ┌────────────────────────────────────────────────────────────────────┐    │
│  │              SECURITY FILTER CHAIN                                   │    │
│  │  SecurityConfig.securityFilterChain()                                │    │
│  │                                                                      │    │
│  │  Order of Execution:                                                 │    │
│  │  1. CORS Filter                                                      │    │
│  │  2. CSRF Filter (disabled)                                          │    │
│  │  3. JWT Authentication Filter ← YOUR CUSTOM FILTER                   │    │
│  │  4. UsernamePasswordAuthenticationFilter                             │    │
│  │  5. Authorization Filter                                             │    │
│  └──────────────────────────────┬─────────────────────────────────────┘    │
│                                  │                                           │
│                                  ▼                                           │
│  ┌────────────────────────────────────────────────────────────────────┐    │
│  │          JWT AUTHENTICATION FILTER                                  │    │
│  │  JwtAuthenticationFilter.doFilterInternal()                          │    │
│  │                                                                      │    │
│  │  Step 1: Extract Token                                              │    │
│  │  ┌──────────────────────────────────────────────────────────────┐  │    │
│  │  │ String token = extractTokenFromRequest(request);              │  │    │
│  │  │ // Checks: Authorization header → X-Access-Token header       │  │    │
│  │  │ // Returns: "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."         │  │    │
│  │  └──────────────────────────────────────────────────────────────┘  │    │
│  │                                                                      │    │
│  │  Step 2: Extract DeviceId from Token                               │    │
│  │  ┌──────────────────────────────────────────────────────────────┐  │    │
│  │  │ String deviceId = jwtTokenUtil.getUsernameFromToken(token);  │  │    │
│  │  │ // Token subject contains deviceId                           │  │    │
│  │  │ // Returns: "device-12345"                                  │  │    │
│  │  └──────────────────────────────────────────────────────────────┘  │    │
│  │                                                                      │    │
│  │  Step 3: Load UserDetails                                           │    │
│  │  ┌──────────────────────────────────────────────────────────────┐  │    │
│  │  │ UserDetails userDetails =                                     │  │    │
│  │  │     jwtUserDetailsService.loadUserByUsername(deviceId);      │  │    │
│  │  │ // Loads user details for authentication                     │  │    │
│  │  └──────────────────────────────────────────────────────────────┘  │    │
│  │                                                                      │    │
│  │  Step 4: Validate Token                                            │    │
│  │  ┌──────────────────────────────────────────────────────────────┐  │    │
│  │  │ boolean isValid = jwtTokenUtil.validateToken(token, userDetails);││
│  │  │ // Validates: signature, expiration, deviceId match           │  │    │
│  │  └──────────────────────────────────────────────────────────────┘  │    │
│  │                                                                      │    │
│  │  Step 5: Create Authentication Object                               │    │
│  │  ┌──────────────────────────────────────────────────────────────┐  │    │
│  │  │ UsernamePasswordAuthenticationToken authentication =          │  │    │
│  │  │     new UsernamePasswordAuthenticationToken(                  │  │    │
│  │  │         userDetails,        // Principal                     │  │    │
│  │  │         null,               // Credentials                   │  │    │
│  │  │         userDetails.getAuthorities() // Authorities          │  │    │
│  │  │     );                                                        │  │    │
│  │  │ authentication.setAuthenticated(true);                        │  │    │
│  │  └──────────────────────────────────────────────────────────────┘  │    │
│  │                                                                      │    │
│  │  Step 6: Set Security Context                                      │    │
│  │  ┌──────────────────────────────────────────────────────────────┐  │    │
│  │  │ SecurityContext context =                                     │  │    │
│  │  │     SecurityContextHolder.getContext();                      │  │    │
│  │  │ context.setAuthentication(authentication);                    │  │    │
│  │  │                                                               │  │    │
│  │  │ // Now available throughout the request:                     │  │    │
│  │  │ // SecurityContextHolder.getContext().getAuthentication()    │  │    │
│  │  └──────────────────────────────────────────────────────────────┘  │    │
│  └──────────────────────────────┬─────────────────────────────────────┘    │
│                                   │                                          │
│                                   ▼                                          │
│  ┌────────────────────────────────────────────────────────────────────┐    │
│  │              AUTHORIZATION CHECK                                    │    │
│  │  SecurityConfig.authorizeHttpRequests()                             │    │
│  │                                                                      │    │
│  │  • Check if endpoint requires authentication                        │    │
│  │  • Verify user has required authorities/roles                      │    │
│  │  • Allow/deny request                                               │    │
│  └──────────────────────────────┬─────────────────────────────────────┘    │
│                                  │                                           │
└──────────────────────────────────┼───────────────────────────────────────────┘
                                   │
                                   ▼
┌─────────────────────────────────────────────────────────────────────────────┐
│                    CONTROLLER LAYER                                         │
│  AuthController.getUserDetails()                                             │
│                                                                              │
│  ┌────────────────────────────────────────────────────────────────────┐    │
│  │  Option 1: Inject Authentication via @AuthenticationPrincipal     │    │
│  │  ┌──────────────────────────────────────────────────────────────┐  │    │
│  │  │ @PostMapping("/userDetails")                                  │  │    │
│  │  │ public ResponseEntity<CommonResponse> getUserDetails(        │  │    │
│  │  │     @Valid @DecryptBody(UserDetailsRequest.class) request,    │  │    │
│  │  │     @AuthenticationPrincipal UserDetails userDetails          │  │    │
│  │  │ ) {                                                            │  │    │
│  │  │     // userDetails.getUsername() = "device-12345"             │  │    │
│  │  │     // No manual token extraction needed!                      │  │    │
│  │  │ }                                                              │  │    │
│  │  └──────────────────────────────────────────────────────────────┘  │    │
│  │                                                                      │    │
│  │  Option 2: Access SecurityContext Directly                         │    │
│  │  ┌──────────────────────────────────────────────────────────────┐  │    │
│  │  │ Authentication auth = SecurityContextHolder                  │  │    │
│  │  │     .getContext()                                            │  │    │
│  │  │     .getAuthentication();                                    │  │    │
│  │  │ UserDetails userDetails = (UserDetails) auth.getPrincipal();│  │    │
│  │  │ String deviceId = userDetails.getUsername();                  │  │    │
│  │  └──────────────────────────────────────────────────────────────┘  │    │
│  └──────────────────────────────┬─────────────────────────────────────┘    │
│                                  │                                           │
└──────────────────────────────────┼───────────────────────────────────────────┘
                                   │
                                   ▼
┌─────────────────────────────────────────────────────────────────────────────┐
│                    SERVICE LAYER                                            │
│  AuthServiceImpl.getUserDetails()                                           │
│                                                                              │
│  ┌────────────────────────────────────────────────────────────────────┐    │
│  │  Access SecurityContext:                                          │    │
│  │  ┌──────────────────────────────────────────────────────────────┐  │    │
│  │  │ SecurityContext context = SecurityContextHolder.getContext();│  │    │
│  │  │ Authentication auth = context.getAuthentication();            │  │    │
│  │  │                                                               │  │    │
│  │  │ if (auth == null || !auth.isAuthenticated()) {               │  │    │
│  │  │     return error("Authentication required");                  │  │    │
│  │  │ }                                                             │  │    │
│  │  │                                                               │  │    │
│  │  │ UserDetails userDetails = (UserDetails) auth.getPrincipal(); │  │    │
│  │  │ String deviceId = userDetails.getUsername();                 │  │    │
│  │  │                                                               │  │    │
│  │  │ // Extract username from authentication details              │  │    │
│  │  │ Map<String, Object> details = (Map) auth.getDetails();      │  │    │
│  │  │ String username = (String) details.get("username");           │  │    │
│  │  └──────────────────────────────────────────────────────────────┘  │    │
│  │                                                                      │    │
│  │  Business Logic:                                                    │    │
│  │  • Validate userId header matches deviceId                         │    │
│  │  • Validate request userId matches token username                  │    │
│  │  • Fetch user from database                                        │    │
│  │  • Generate refreshed token                                        │    │
│  │  • Return user details                                             │    │
│  └──────────────────────────────┬─────────────────────────────────────┘    │
│                                  │                                           │
└──────────────────────────────────┼───────────────────────────────────────────┘
                                   │
                                   ▼
┌─────────────────────────────────────────────────────────────────────────────┐
│                    RESPONSE                                                 │
│  CommonResponse with user details                                           │
│  Headers: X-Access-Token (refreshed token)                                 │
└─────────────────────────────────────────────────────────────────────────────┘
```

---

## Security Context Lifecycle

```
┌─────────────────────────────────────────────────────────────────────────┐
│                    REQUEST ARRIVES                                        │
│  HttpServletRequest enters filter chain                                   │
└──────────────────────────────┬──────────────────────────────────────────┘
                                │
                                ▼
┌─────────────────────────────────────────────────────────────────────────┐
│              SECURITY CONTEXT CREATION                                   │
│                                                                           │
│  Thread: Request Thread                                                  │
│  SecurityContext: ThreadLocal<SecurityContext>                            │
│                                                                           │
│  ┌─────────────────────────────────────────────────────────────────┐    │
│  │  SecurityContextHolder.getContext()                              │    │
│  │  │                                                               │    │
│  │  │  ThreadLocal Storage:                                        │    │
│  │  │  ┌─────────────────────────────────────────────────────┐    │    │
│  │  │  │  Thread: http-nio-8080-exec-1                        │    │    │
│  │  │  │  SecurityContext: {                                   │    │    │
│  │  │  │    authentication: UsernamePasswordAuthenticationToken│    │    │
│  │  │  │  }                                                    │    │    │
│  │  │  └─────────────────────────────────────────────────────┘    │    │
│  │  │                                                               │    │
│  │  │  Thread: http-nio-8080-exec-2                              │    │
│  │  │  SecurityContext: { ... } (different context)              │    │
│  │  └─────────────────────────────────────────────────────────────┘    │
│  │                                                                       │
│  │  Key Points:                                                         │
│  │  • Each request thread has its own SecurityContext                 │
│  │  • Thread-safe: No interference between concurrent requests         │
│  │  • Automatically cleared after request completes                    │
│  └─────────────────────────────────────────────────────────────────────┘
└──────────────────────────────┬──────────────────────────────────────────┘
                                │
                                ▼
┌─────────────────────────────────────────────────────────────────────────┐
│              AUTHENTICATION SET                                          │
│  JWT Filter sets authentication in SecurityContext                       │
│                                                                           │
│  SecurityContextHolder.getContext().setAuthentication(authentication);    │
│                                                                           │
│  Authentication Object:                                                  │
│  {                                                                        │
│    principal: UserDetails (deviceId, authorities)                      │
│    credentials: null                                                      │
│    authorities: [ROLE_USER, ...]                                          │
│    authenticated: true                                                    │
│    details: {                                                             │
│      username: "user@example.com",                                       │
│      deviceId: "device-12345",                                           │
│      token: "eyJhbGci..."                                                │
│    }                                                                      │
│  }                                                                        │
└──────────────────────────────┬──────────────────────────────────────────┘
                                │
                                ▼
┌─────────────────────────────────────────────────────────────────────────┐
│              AVAILABLE THROUGHOUT REQUEST                               │
│  Controller, Service, Repository can access:                            │
│                                                                           │
│  SecurityContextHolder.getContext().getAuthentication()                  │
│                                                                           │
│  • Controller: @AuthenticationPrincipal                                  │
│  • Service: SecurityContextHolder.getContext()                           │
│  • Repository: SecurityContextHolder.getContext()                        │
│  • Any layer: Direct access to authentication                            │
└──────────────────────────────┬──────────────────────────────────────────┘
                                │
                                ▼
┌─────────────────────────────────────────────────────────────────────────┐
│              REQUEST COMPLETES                                           │
│  Response sent to client                                                 │
│                                                                           │
│  SecurityContext automatically cleared:                                   │
│  • ThreadLocal cleaned up                                                │
│  • Authentication removed                                                │
│  • Ready for next request                                                │
└─────────────────────────────────────────────────────────────────────────┘
```

---

## Comparison: Before vs After

### BEFORE (Manual Token Extraction)

```
Controller
  │
  ├─→ Service.getUserDetails()
  │     │
  │     ├─→ getTokenFromRequest()          ← Manual extraction
  │     │     └─→ Read Authorization header
  │     │
  │     ├─→ jwtTokenUtil.validateToken()   ← Manual validation
  │     │
  │     ├─→ jwtTokenUtil.getUsernameFromToken() ← Manual extraction
  │     │
  │     └─→ Business Logic
  │
  └─→ Response
```

**Issues:**
- ❌ Duplicate token extraction code in every service method
- ❌ Manual validation in each endpoint
- ❌ No centralized authentication logic
- ❌ Hard to test (need to mock request headers)
- ❌ Easy to forget validation in new endpoints

### AFTER (Security Context)

```
Request
  │
  ├─→ JWT Filter (automatic)
  │     ├─→ Extract token
  │     ├─→ Validate token
  │     └─→ Set SecurityContext
  │
  ├─→ Controller
  │     └─→ Service.getUserDetails()
  │           │
  │           ├─→ SecurityContextHolder.getContext() ← Direct access
  │           │     └─→ Get authentication
  │           │
  │           └─→ Business Logic
  │
  └─→ Response
```

**Benefits:**
- ✅ Centralized authentication (one filter)
- ✅ Automatic validation for all protected endpoints
- ✅ Clean code (no manual extraction)
- ✅ Easy to test (mock SecurityContext)
- ✅ Consistent security across all endpoints

---

## Key Components Diagram

```
┌─────────────────────────────────────────────────────────────────────┐
│                    SPRING SECURITY COMPONENTS                       │
│                                                                      │
│  ┌──────────────────────────────────────────────────────────────┐  │
│  │  SecurityConfig                                               │  │
│  │  • Configures SecurityFilterChain                             │  │
│  │  • Defines public/protected endpoints                         │  │
│  │  • Sets up CORS, CSRF, session management                    │  │
│  └──────────────────────────────┬───────────────────────────────┘  │
│                                   │                                  │
│                                   ▼                                  │
│  ┌──────────────────────────────────────────────────────────────┐  │
│  │  JwtAuthenticationFilter                                      │  │
│  │  • Extracts JWT token from request                            │  │
│  │  • Validates token                                            │  │
│  │  • Creates Authentication object                              │  │
│  │  • Sets SecurityContext                                       │  │
│  └──────────────────────────────┬───────────────────────────────┘  │
│                                   │                                  │
│                                   ▼                                  │
│  ┌──────────────────────────────────────────────────────────────┐  │
│  │  SecurityContextHolder                                         │  │
│  │  • ThreadLocal storage for SecurityContext                    │  │
│  │  • Provides getContext() method                              │  │
│  │  • Thread-safe per request                                    │  │
│  └──────────────────────────────┬───────────────────────────────┘  │
│                                   │                                  │
│                                   ▼                                  │
│  ┌──────────────────────────────────────────────────────────────┐  │
│  │  SecurityContext                                              │  │
│  │  • Contains Authentication object                              │  │
│  │  • Available throughout request lifecycle                     │  │
│  │  • Automatically cleared after request                        │  │
│  └──────────────────────────────┬───────────────────────────────┘  │
│                                   │                                  │
│                                   ▼                                  │
│  ┌──────────────────────────────────────────────────────────────┐  │
│  │  Authentication                                                │  │
│  │  • Principal: UserDetails                                     │  │
│  │  • Authorities: Roles/permissions                            │  │
│  │  • Details: Custom data (username, deviceId, etc.)           │  │
│  │  • Authenticated: true/false                                 │  │
│  └──────────────────────────────────────────────────────────────┘  │
└──────────────────────────────────────────────────────────────────────┘
```

---

## Public vs Protected Endpoints

```
┌─────────────────────────────────────────────────────────────────────┐
│                    ENDPOINT CLASSIFICATION                          │
│                                                                      │
│  ┌──────────────────────────────────────────────────────────────┐  │
│  │  PUBLIC ENDPOINTS (No Authentication)                       │  │
│  │  • /api/v1/auth/login/**                                    │  │
│  │  • /api/v1/auth/member/requestOtp                           │  │
│  │  • /api/v1/auth/nonmember/requestOtp                        │  │
│  │  • /api/v1/auth/verifyOtp                                   │  │
│  │  • /api/v1/auth/register/**                                 │  │
│  │  • /api/v1/auth/biometric/challenge                         │  │
│  │  • /api/v1/generateToken                                    │  │
│  │  • /actuator/**                                             │  │
│  │                                                              │  │
│  │  JWT Filter: shouldNotFilter() returns true                │  │
│  │  → Filter skipped, no authentication required               │  │
│  └──────────────────────────────────────────────────────────────┘  │
│                                                                      │
│  ┌──────────────────────────────────────────────────────────────┐  │
│  │  PROTECTED ENDPOINTS (Authentication Required)               │  │
│  │  • /api/v1/auth/userDetails                                  │  │
│  │  • /api/v1/auth/biometric/login                              │  │
│  │  • /api/v1/auth/biometric                                   │  │
│  │  • All other endpoints                                       │  │
│  │                                                              │  │
│  │  JWT Filter: shouldNotFilter() returns false                │  │
│  │  → Filter executes, validates token                          │  │
│  │  → Sets SecurityContext if valid                             │  │
│  │  → Returns 401 if invalid/missing                            │  │
│  └──────────────────────────────────────────────────────────────┘  │
└──────────────────────────────────────────────────────────────────────┘
```

---

## Error Scenarios

```
┌─────────────────────────────────────────────────────────────────────┐
│                    ERROR HANDLING FLOW                              │
│                                                                      │
│  Scenario 1: Missing Token                                         │
│  ┌──────────────────────────────────────────────────────────────┐  │
│  │  Request: No Authorization header, no X-Access-Token          │  │
│  │  → JWT Filter: extractTokenFromRequest() returns null         │  │
│  │  → SecurityContext: No authentication set                     │  │
│  │  → Authorization Filter: .authenticated() fails              │  │
│  │  → Response: 401 Unauthorized                                 │  │
│  └──────────────────────────────────────────────────────────────┘  │
│                                                                      │
│  Scenario 2: Invalid Token                                         │
│  ┌──────────────────────────────────────────────────────────────┐  │
│  │  Request: Authorization: Bearer invalid_token                 │  │
│  │  → JWT Filter: Extracts token                                 │  │
│  │  → JWT Filter: validateToken() returns false                  │  │
│  │  → SecurityContext: No authentication set                     │  │
│  │  → Authorization Filter: .authenticated() fails              │  │
│  │  → Response: 401 Unauthorized                                 │  │
│  └──────────────────────────────────────────────────────────────┘  │
│                                                                      │
│  Scenario 3: Expired Token                                         │
│  ┌──────────────────────────────────────────────────────────────┐  │
│  │  Request: Authorization: Bearer expired_token                 │  │
│  │  → JWT Filter: Extracts token                                 │  │
│  │  → JWT Filter: validateToken() throws ExpiredJwtException    │  │
│  │  → SecurityContext: No authentication set                     │  │
│  │  → Authorization Filter: .authenticated() fails              │  │
│  │  → Response: 401 Unauthorized                                │  │
│  └──────────────────────────────────────────────────────────────┘  │
│                                                                      │
│  Scenario 4: Valid Token                                           │
│  ┌──────────────────────────────────────────────────────────────┐  │
│  │  Request: Authorization: Bearer valid_token                   │  │
│  │  → JWT Filter: Extracts token                                 │  │
│  │  → JWT Filter: validateToken() returns true                   │  │
│  │  → SecurityContext: Authentication set                        │  │
│  │  → Authorization Filter: .authenticated() passes              │  │
│  │  → Controller: Can access authentication                      │  │
│  │  → Response: 200 OK with data                                 │  │
│  └──────────────────────────────────────────────────────────────┘  │
└──────────────────────────────────────────────────────────────────────┘
```

---

This diagram provides a complete visual representation of how Security Context works in the auth service, from request arrival to response, including error scenarios and component interactions.




