Service: membership-service
Controller: VirtualIdFeignProxyController
Original prompt: ramesh-membership-virtualidfeignproxycontroller.md
Scope: Regenerate the entire `membership-service` focused on endpoints that proxy to the Virtual ID system.

Instructions:
1. Inspect `services/membership-service` for controller signatures, Feign clients, DTOs, and tests.
2. Implement controller handlers, Feign client interfaces, service layer, DTOs, error handling, and tests.
3. Ensure Feign multipart support, timeouts, and error translation are handled.

Requirements:
- Java 17, Spring Boot 3.x, Spring Cloud OpenFeign (if used).
- Preserve domain contracts and package names.
- Include unit and integration tests with mocked Feign clients.

Output:
- Git-style patch with full file contents, tests, and README notes for integration config.

Regenerate now.
