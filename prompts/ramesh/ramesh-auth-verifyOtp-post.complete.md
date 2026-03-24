Service: auth-service
Controller: AuthController
Endpoint: POST /api/v1/auth/verifyOtp
Original prompt: ramesh-auth-verifyOtp-post.md

Purpose:
This prompt must instruct an external code-generation AI to regenerate the entire auth-service (all source files, DTOs, configs, resources, and tests) focused on fully implementing the `verifyOtp` endpoint and its integrations.

Context extraction:
- First, scan the repository path `services/auth-service` to locate `AuthController`, DTO classes (e.g., `VerifyOtpRequest`, responses), service classes, security utilities, and any existing tests. Use the exact package names and existing imports.
- If DTOs or services are missing or incomplete, infer fields from usages in controller and service code. For any inferred DTO provide an explicit JSON schema example in the output.

Endpoint behavior (must be implemented exactly):
- Accepts a JSON request body matching `VerifyOtpRequest` (extract fields from repo). Validate with Jakarta Validation (`@Valid`). The request is decrypted via `@DecryptBody`.
- Performs deviceId validation using `SecurityValidationUtil.validateDeviceId()` before proceeding.
- Delegates verification to `RegistrationService.verifyLoginOtpAndTrustDevice`.
- Returns a ResponseEntity<CommonResponse> with proper token handling via `ResponseHeaderUtil.processTokenResponse`.
- Log request and response at INFO level and handle exceptions with `GlobalExceptionHandler`.

Non-functional requirements:
- Java 17, Spring Boot 3.x
- Preserve `@EncryptResponse` / `@DecryptBody` annotations and any custom crypto behavior.
- Keep existing package names and build files (Maven/Gradle).
- Include unit tests for the controller and service (mock dependencies) and an integration test that runs with an in-memory profile.
- Ensure security: validate headers, device id, and avoid logging sensitive fields (mask if necessary).

Output format:
- Provide a git-style patch that adds/updates all necessary files. For each file include full contents and path.
- If you add/modify DTOs, show both Java class and an example JSON request/response.
- Mark any breaking changes and migration steps clearly.

Regenerate now and output only the git-style patch.
