Service: auth-service
Controller: AuthController
Endpoint: POST /api/v1/auth/validateMemberDetails
Original prompt: ramesh-auth-validateMemberDetails-post.md

Purpose:
Regenerate the `auth-service` to fully implement `validateMemberDetails` endpoint and surrounding code.

Context extraction:
- Inspect `services/auth-service` for DTOs (`MemberRegistrationRequest`), controllers, services, validators, and tests. Extract exact DTO fields if present.
- If fields are missing, infer reasonable fields and include JSON schema examples.

Endpoint behavior:
- Accepts `MemberRegistrationRequest` via `@DecryptBody` and `@Valid`.
- Calls `RegistrationService.registerMember` to perform registration logic.
- Returns `CommonResponse` with `@EncryptResponse`.
- Validate inputs, sanitize strings, and ensure no PII is logged.

Requirements:
- Java 17, Spring Boot 3.x
- Preserve annotations, packaging, and build configuration.
- Provide unit tests (controller/service) and an integration test using in-memory DB or mocked dependencies.

Output:
- Provide git-style patch with full file contents for all new/modified files and a migration note for breaking changes.

Regenerate now.
