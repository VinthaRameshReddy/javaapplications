# Prompt: Membership + Shared Membership Full API Doc

Date: 2026-02-10

Goal
Create a single, complete API documentation file that combines `membership-service` and `shared-membership-service`, including all related Feign calls (shared-utilization-service and shared-virtualid-service). The output must match the style of `services/claims-service/docs/00-claims-to-shared-reimbursement-implementation.md`.

Scope to inspect
- `services/membership-service`
  - Controllers:
    - `MembershipProxyController`
    - `UtilizationProxyController`
    - `VirtualIdFeignProxyController`
  - Feign clients:
    - `SharedMembershipServiceClient`
    - `SharedUtilizationServiceClient`
    - `SharedVirtualIdServiceClient`
  - Security + error handling:
    - `SecurityConfig`
    - `MemberCodeValidationService`
    - `GlobalExceptionHandler`
  - Models:
    - `UtilizationRequest`
    - `VirtualIdResponseDto`
  - Config:
    - `application.properties`
    - `FeignClientConfig`

- `services/shared-membership-service`
  - Controller:
    - `MemberController`
  - Service implementation:
    - `MemberServiceImpl`
  - Repositories:
    - `MembershipRepository`
    - `MaternityBenefitsRepository`
    - `DataPrivacyTaggingRepository`
  - Models/responses:
    - `MembershipResponse`
    - `UserDependentResponse`
    - `MaternityBenefitsResponse`
    - `MembershipEntity`
    - `MaternityBenefitsEntity`
  - Security:
    - `SecurityConfig`
  - Config:
    - `application-staging.properties`

Required content (single file)
- Title, purpose, and high-level architecture diagram
- Base paths for both services
- End-to-end runtime flow
- All membership-service endpoints + downstream mappings
- All shared-membership-service endpoints
- Feign mappings (paths + method signatures)
- Request/response models (CommonResponse, ErrorResponse, UtilizationRequest, MembershipResponse, UserDependentResponse, MaternityBenefitsResponse, VirtualIdResponseDto)
- Error handling mapping for both services
- Configuration notes (context paths, Feign URLs, security)
- Note any commented-out or non-exposed code (e.g., file management proxy/client)

Output location
- `services/membership-service/docs/00-membership-to-shared-membership-implementation.md`

Format constraints
- Use the same structure and tone as the claims-service full document
- Use concise headings with numbered sections where helpful
- Include mermaid diagram if applicable
