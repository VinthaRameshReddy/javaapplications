Service: auth-service
Controller: AuthController
Endpoint: POST /api/v1/auth/validateMemberRegDetails
Original prompt: ramesh-auth-validateMemberRegDetails-post.md

Purpose:
Regenerate the entire `auth-service` with a complete, production-ready implementation of `validateMemberRegDetails`.

Context extraction:
- Scan `services/auth-service` for `AuthController`, DTOs (e.g., `MemberValidationRequest`), `RegistrationService`, and existing validators.
- If DTOs are not present or incomplete, infer fields from usage and provide explicit JSON schema examples.

Endpoint behavior:
- Accept `MemberValidationRequest` via `@DecryptBody`, validate with `@Valid`.
- Delegate validation logic to `RegistrationService.validateMemberRegistration`.
- Return `CommonResponse` wrapped and encrypted via `@EncryptResponse`.
- Handle validation and runtime exceptions with global exception handler.

Requirements:
- Java 17, Spring Boot 3.x
- Preserve crypto annotations, package structure, and logging.
- Include unit and integration tests (mocking external dependencies).
- Output a git-style patch applying all necessary file changes.

Deliverable:
- Git-style patch with updated/added Java files, tests, resource files, and README notes for any breaking changes.

Regenerate now.
