## Task

Update the `setPasswordMember` endpoint in `auth-service` so that the successful response `data` object only contains a `message` field and does not expose any user personal data.

## Requirements

- **Endpoint in scope**: `/api/v1/auth/setPasswordMember` in `services/auth-service`.
- **Current behavior**: On successful password set, the API returns a `CommonResponse` with `data` containing fields like:
  - `memberCode`
  - `firstName`
  - `sex`
  - `birthDate`
  - `email` (and/or phone/mobile)
- **New behavior**:
  - Keep the overall response structure the same (`statusCode`, `response`, `data`, `traceId`, etc.).
  - Inside `data`, **remove all user‑specific fields** and **only return** a non‑PII `message` field, e.g.:
    ```json
    {
      "statusCode": "200",
      "response": "SUCCESS",
      "data": {
        "message": "Password set successfully"
      },
      "traceId": "545"
    }
    ```

## Constraints

- **Do not change or remove any existing business logic**:
  - Keep all validations as they are.
  - Do not change how the password is encoded or saved.
  - Do not change how user data is read from OTP/session.
  - Do not change OTP cleanup behavior.
  - Do not change welcome‑email sending or any other side effects.
- Only modify how the **success response payload is built** for `setPasswordMember` so that it no longer includes PII (member code, name, sex, birth date, email/phone) in the `data` section.

## Acceptance Criteria

- Calling `/api/v1/auth/setPasswordMember` with valid input:
  - Still successfully sets the password and triggers all existing side effects.
  - Returns `CommonResponse` with:
    - `response = "SUCCESS"`
    - `statusCode = "200"` (or existing success code)
    - `data` containing **only** the `message` field.
- No other endpoints or flows are impacted.
