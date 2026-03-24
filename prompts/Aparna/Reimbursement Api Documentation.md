# Prompt: Reimbursement API Documentation

Date: 2026-02-10

Goal
Create a single, complete API documentation file that explains how `claims-service` proxies reimbursement flows to `shared-reimbursement-service` via Feign. The output must match the style of `services/loa-service/docs/00-loa-service-full-document.md` and include step-by-step implementation details plus code snippets.

Scope to inspect
- `services/claims-service`
  - Controller:
    - `ReimbursementProxyController`
  - Feign client:
    - `ReimbursementsServiceClient`
  - Security + request validation:
    - `SecurityConfig`
    - `MemberCodeValidationService`
  - Reference data services:
    - `ClaimNatureService`, `ClaimNatureServiceImpl`
    - `BankMasterService`, `BankMasterServiceImpl`
    - `BankAccountValidationService`
  - Config:
    - `FeignClientConfig`
    - `FeignMultipartSupportConfig`
    - `application.properties`

- `services/shared-reimbursement-service`
  - Controller:
    - `ReimbursementController`
  - Core processing:
    - `ReimbursementsServiceImpl`
    - `ReimDbService`
  - Document rules:
    - `ReimbursementDocumentService`
    - `FileValidationService`
  - Security + outbound:
    - `SecurityConfig`
    - `FeignClientConfig`
    - `NotificationFeignConfig`
  - External clients:
    - `FileManagementServiceClient`
    - `MembershipServiceClient`
    - `NotificationServiceClient`
  - Config:
    - `application.properties`

Required content (single file)
- Title, purpose, and high-level architecture diagram (Mermaid)
- Base paths for both services
- End-to-end runtime flow (JWT + Basic/API key + Feign proxy)
- All claims-service endpoints and their downstream mappings
- All shared-reimbursement-service endpoints
- Feign mappings (paths + method signatures)
- Request/response models (core DTOs only)
- Document upload rules and validation (required docs matrix)
- Error handling and transactional notes (dual DB handling)
- Configuration notes (context paths, Feign URLs, security, encryption)

Output location
- `services/claims-service/docs/00-claims-to-shared-reimbursement-implementation.md`

Format constraints
- Use the same structure and tone as the LOA full document
- Use concise headings with numbered sections where helpful
- Include Mermaid diagram for the architecture
