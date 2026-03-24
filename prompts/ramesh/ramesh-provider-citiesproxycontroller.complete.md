Service: provider-service
Controller: CitiesProxyController
Original prompt: ramesh-provider-citiesproxycontroller.md
Scope: Regenerate the entire `provider-service` focusing on city proxy endpoints.

Instructions:
1. Inspect `services/provider-service` for controller signatures, DTOs, Feign clients, and config. Extract exact method signatures if present.
2. Implement controller, service, Feign clients, error handling, caching, and tests.
3. Ensure request parameters and response shapes exactly match upstream contracts (`/provider/v1/cities` etc.).

Requirements:
- Java 17, Spring Boot 3.x, Spring Cloud OpenFeign if used.
- Preserve headers forwarding, security and validation.
- Include unit and integration tests with mocked downstream dependencies.

Output:
- Git-style patch with full file contents for all files required to build and test the service.

Regenerate now.
