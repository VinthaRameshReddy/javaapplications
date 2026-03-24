# Email Validation Endpoint for Auth Service

## Endpoint
`GET /api/v1/auth/email/validate?email=example@example.com`

## Description
This endpoint validates the format of an email address using the service's internal validation logic. It returns whether the email is valid or not.

- **Service:** EmailValidationService
- **Controller:** EmailValidationController
- **Response:** `Email valid: true` or `Email valid: false`

## Example Request
```
GET /api/v1/auth/email/validate?email=test@example.com
```

## Example Response
```
Email valid: true
```

## Notes
- The validation uses the same logic as the internal EmailValidator class.
- No authentication is required for this endpoint (can be adjusted as needed).

