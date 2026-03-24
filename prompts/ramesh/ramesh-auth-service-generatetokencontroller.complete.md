Service: auth-service
Controller: GenerateTokenController
Original prompt: ramesh-auth-service-generatetokencontroller.md
Scope: Regenerate the token generation endpoints and any supporting utilities.

Instructions:
1. Extract controller method signatures, token formats, expiration policies, and any existing token utilities from `services/auth-service`.
2. Implement secure token generation, validation, refresh, and revocation flows. Preserve token formats and compatibility.
3. Provide unit tests for token generation logic and integration tests for endpoints that emit tokens.

Requirements:
- Java 17, Spring Boot 3.x
- Use existing security libraries or implement JWT signing with proper key management.
- Output a git-style patch with full file contents, tests, and README on key rotation/migration if changed.

Regenerate now.
