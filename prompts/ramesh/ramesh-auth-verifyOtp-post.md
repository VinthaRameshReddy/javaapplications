Service: auth-service
Controller: AuthController
Endpoint: POST /api/v1/auth/verifyOtp

Goal:
Regenerate the entire `auth-service` (all source files, DTOs, configs, resources, and tests) so this endpoint is fully implemented and integrated, including token response handling.

Requirements:
- Java 17, Spring Boot 3.x
- Preserve package names, encryption/decryption annotations, validation, and logging.
- Include tests covering success and failure OTP flows and token headers.
- Output a git-style patch containing all updated/added files.

Regenerate now.
