Service: membership-service
Controller: MembershipProxyController
Original prompt: ramesh-membership-membershipproxycontroller.md
Scope: Regenerate membership-service to fully implement membership proxy endpoints.

Instructions:
1. Scan `services/membership-service` to extract controller methods, DTOs, Feign clients, and service implementations.
2. Implement controllers, service layer, DTOs, repositories (if applicable), and tests.
3. Ensure proxy semantics (forwarding headers, error translation) are implemented consistently.

Requirements:
- Java 17, Spring Boot 3.x
- Preserve Feign contracts and package organization.
- Include unit tests and an integration test suite using mocked downstream services.

Output:
- Git-style patch with all updated and new files.

Regenerate now.
