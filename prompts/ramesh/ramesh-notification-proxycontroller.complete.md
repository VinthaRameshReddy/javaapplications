Service: notification-service
Controller: NotificationProxyController
Original prompt: ramesh-notification-proxycontroller.md
Scope: Regenerate the entire `notification-service` with full implementation of all endpoints in `NotificationProxyController`.

Instructions:
1. Inspect `services/notification-service` to extract controller signatures, DTOs, messaging clients, and integrations. Use exact package names and existing client configurations.
2. Implement controllers, services, DTOs, adapters for downstream transports (HTTP, message brokers), error handling, and retry/circuit-breaker patterns if applicable.
3. Include unit and integration tests. Provide local dev instructions and sample cURL requests.

Requirements:
- Java 17, Spring Boot 3.x
- Preserve security, encryption, and contract compatibility with downstream systems.
- Output a git-style patch with full file contents and test coverage.

Regenerate now.
