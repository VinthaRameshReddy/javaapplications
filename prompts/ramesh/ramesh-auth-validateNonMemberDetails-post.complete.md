Service: auth-service
Controller: AuthController
Endpoint: POST /api/v1/auth/validateNonMemberDetails
Original prompt: ramesh-auth-validateNonMemberDetails-post.md

Purpose:
Regenerate the `auth-service`, implementing `validateNonMemberDetails` thoroughly.

Context extraction:
- Scan `services/auth-service` for `NonMemberRegistrationRequest` DTO and registration flows. Extract fields when available; otherwise infer and include JSON schema examples.

Endpoint behavior:
- Accepts `NonMemberRegistrationRequest` via `@DecryptBody`, validate with `@Valid`, call `RegistrationService.registerNonMember`.
- Return encrypted `CommonResponse`.
- Ensure validation rules (email/phone formats, required fields) are enforced and documented.

Requirements:
- Java 17, Spring Boot 3.x; preserve package structure and crypto annotations.
- Unit and integration tests; input validation and security checks.

Output:
- Git-style patch with all modified/added files and test suites.

Regenerate now.
