Service: reimbursement-service
Controller: ReimbursementController
Original prompt: ramesh-reimbursement-service-reimbursement.md
Scope: Regenerate the entire `reimbursement-service` focusing on endpoints declared in `ReimbursementController`.

Instructions:
1. Scan `services/reimbursement-service` to extract controller method signatures, DTOs, services, and tests.
2. Implement controllers, service layer, DTOs, repositories (if any), error handling, and tests that match existing behavior.
3. Preserve encryption annotations and response wrappers; include integration tests using in-memory DB or mocked dependencies.

Behavior:
- GET /api/v1/history?memberCode=
- Implement pagination if large results expected.
- Ensure proper mapping to `ReimHistoryDTO` and return `CommonResponse.success(...)`.

Output:
- Git-style patch with full file contents and tests.

Regenerate now.
