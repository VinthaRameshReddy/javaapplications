Service: loa-service
Controller: LoaController
Original prompt: ramesh-loa-loacontroller.md
Scope: Regenerate the entire `loa-service` focusing on all endpoints declared in `LoaController`. Produce full service code, DTOs, repositories, configs, resources, and tests.

Instructions for code-generation AI:
1. Scan `services/loa-service` to extract controller method signatures, DTOs, domain models, and existing business rules. Use exact package names.
2. Implement controller handlers with proper validation, logging, and exception handling. Preserve or improve existing annotations.
3. Implement service layer, repository interfaces (JPA or alternatives), DTOs, mapping, and unit/integration tests.
4. If DTOs or persistence models are missing, infer fields from controller/service usage and include JSON schema examples.

Behavioral requirements:
- Follow domain rules present in code or infer sensible defaults; validate dates, statuses, and permissions correctly.
- Use Java 17, Spring Boot 3.x; produce a working Maven/Gradle build.
- Provide clear error responses (standardized CommonResponse) and include unit tests covering success and error flows.

Output:
- A git-style patch containing all new/updated files, tests, and migration notes for any breaking changes.

Regenerate now.
