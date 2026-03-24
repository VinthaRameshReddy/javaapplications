Service: auth-service
Controller: AuthController
Original prompt: ramesh-auth-service-authcontroller.md
Scope: Regenerate entire auth-service implementing all endpoints in `AuthController` with full behavior and fields.

Instructions:
1. Scan `services/auth-service` for controllers, DTOs, services, utils, and tests. Extract exact method signatures and DTO field definitions where present.
2. For each endpoint in `AuthController`, generate complete implementations: controllers, services, DTOs, validators, security utilities, and tests.
3. Include detailed JSON schema examples for each request/response DTO and preserve/defer to existing crypto annotations.

Behavior requirements:
- Device ID validation, token handling, encryption/decryption, logging, and error handling as used in the repo.
- Avoid logging sensitive data. Include rate-limiting guidance for OTP endpoints.

Output:
- Single git-style patch that recreates the auth-service completely (source code, tests, configs, resources). Mark breaking changes.

Regenerate now.
