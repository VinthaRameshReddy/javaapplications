Service: facescan-service
Controller: FaceScanController
Original prompt: ramesh-facescan-facecontroller.md
Scope: Regenerate the entire `facescan-service` ensuring full implementation of `FaceScanController` endpoints and biometric flows.

Instructions:
1. Scan `services/facescan-service` to extract controller signatures, DTOs, biometric clients, and any existing integration code.
2. Implement controllers, services, adapters to biometric providers, encryption/decryption, and validation.
3. Provide unit tests and integration tests (mock biometric provider) and include example requests/responses.

Behavior:
- Preserve encryption, input validation, logging. Ensure biometric data is handled securely and PII is never logged.
- Use Java 17, Spring Boot 3.x. Keep package names and build files intact.

Output:
- Git-style patch containing all files needed to build and test the service.

Regenerate now.
