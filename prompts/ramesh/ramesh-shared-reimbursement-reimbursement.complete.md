Service: shared-reimbursement-service
Controller: ReimbursementController
Original prompt: ramesh-shared-reimbursement-reimbursement.md
Scope: Regenerate the entire `shared-reimbursement-service` for all endpoints in `ReimbursementController`.

Instructions:
1. Inspect `services/shared-reimbursement-service` for controller signatures, DTOs, file handling utilities, and services.
2. Implement controller, service layer, file validation, document building, multipart handling, and tests.
3. Preserve `@EncryptResponse` / `@DecryptBody` annotations and ensure secure handling of uploaded files.

Behavior to implement:
- GET /v1/history?memberCode=
- POST /v1/viewAmount
- POST /v1/submitReimbursement (multipart/form-data) — preserve file sections and validation rules
- POST /v1/resubmitReimbursement (multipart/form-data)
- GET /v1/images/reimbursement-success.jpg — serve classpath resource with caching headers

Non-functional:
- Java 17, Spring Boot 3.x
- Unit tests for file validation and submission flows; integration test for multipart endpoint.

Output:
- Git-style patch with full file contents, resources (images), and tests. Document breaking changes.

Regenerate now.
