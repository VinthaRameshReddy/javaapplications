# Prompt: LOA API Documentation

Date: 2026-02-10

Goal
Create a single, complete API documentation file for `loa-service`. The output must match the style of `services/loa-service/docs/00-loa-service-full-document.md` and include step-by-step implementation details plus code snippets.

Scope to inspect
- `services/loa-service`
  - Controller:
    - `LoaController`
  - Feign clients:
    - `MedicardApiClient`
    - `MedicardAuthClient`
  - Security + request validation:
    - `SecurityConfig`
    - `JwtRequestFilter` (from shared config)
  - Token management:
    - `TokenService`
  - Config:
    - `FeignClientConfig`
    - `AuthFeignClientConfig`
    - `application.properties`
  - DTOs:
    - `domain.dto.request.*`
    - `domain.dto.response.*`
  - Error handling:
    - `GlobalExceptionHandler`

Required content (single file)
- Title, purpose, and high-level architecture diagram (Mermaid)
- Base path and endpoints
- End-to-end runtime flow (JWT + APIM key + OAuth2 token + Feign proxy)
- Endpoint flow diagrams (Mermaid flowcharts)
- Request/response models (core DTOs only)
- Error handling behavior
- Configuration notes (context paths, URLs, security, encryption)
- Step-by-step implementation with code snippets

Output location
- `services/loa-service/docs/00-loa-service-full-document.md`

Format constraints
- Use the same structure and tone as the LOA full document
- Use concise headings with numbered sections where helpful
- Include Mermaid diagram for the architecture
