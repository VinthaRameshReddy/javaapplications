Service: filemanagement-service
Controller: FileController
Original prompt: ramesh-filemanagement-filecontroller.md
Scope: Regenerate the entire `filemanagement-service` to fully implement file upload/download, tag search, and related endpoints.

Instructions:
1. Scan `services/filemanagement-service` for controller signatures, DTOs, storage adapters, and tests.
2. Implement controller handlers with multipart handling, storage adapters (local, S3-compatible), validation, virus scan hooks (if applicable), and URL signing for downloads.
3. Provide unit/integration tests and sample cURL commands for uploads and downloads.

Requirements:
- Java 17, Spring Boot 3.x
- Preserve package names and build system.
- Securely handle uploaded file metadata and content; validate content-type and size limits.

Output:
- Git-style patch containing all new/updated files and tests.

Regenerate now.
