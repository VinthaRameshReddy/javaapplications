## Reimbursement Admin Email Template Adjustments

You are helping with a Spring Boot microservice project (Java 17, Gradle).  
Path of interest: `services/shared-reimbursement-service/src/main/java/com/medgo/reimburse/service/ReimbursementNotificationService.java`

This service builds HTML email bodies for reimbursement submission notifications and calls a shared notification service via Feign. There are 2 types of recipients:

- **Admin**: `includeBankDetails = true`
- **User**: `includeBankDetails = false`

I need you to **review and, if needed, adjust** the admin email HTML so it matches these exact requirements, **without breaking any existing logic or user-email behavior**.

---

### 1. Admin-only control code text

- For admin emails (`includeBankDetails = true`), show this line:
  - `Your ControlCode is: ABC12345`
- The control code must appear **beside the message on the same line**, not on a new line.
- This line should appear **only once**, and only for admin emails.
- It should be styled similarly to what is already in the file:
  - Normal text color for the label, bold/colored span for the code.
- For user emails (`includeBankDetails = false`):
  - Keep the existing large control code display behavior.
  - **Do not** add the “Your ControlCode is” line for user emails.

Implementation hint:

- In both `buildEmailBodyWithImage(...)` and `buildEmailBodyHtmlOnly(...)`, ensure the admin path renders something like:

```html
<p style="...">
  Your ControlCode is:
  <span style="color: #667eea; font-weight: bold;">
    {{controlCode}}
  </span>
</p>
```

---

### 2. Remove duplicate / “downside” control code for admin

- In admin emails, there should **not** be an extra big control code block “below” the message.
- For admin:
  - Keep only the inline `Your ControlCode is: <code>` line.
  - Remove any second visual repetition of the control code that appears below it.
- For user:
  - The existing big control-code section should stay exactly as it is now.

Concretely:

- Where the template currently builds both:
  - An inline control code text
  - And a separate large `<p>` or block with the control code
- Make that **conditional** so:
  - Admin path: only inline text
  - User path: only the large control code block

---

### 3. Bank details section (admin only)

- For admin emails, above the bank details table, show this exact sentence:
  - `The bank account details you provided for e-payout is:`
- **Do NOT** show the “Bank Account Details” header text anymore (just remove that heading).
- Keep the existing table rows/labels for:
  - **Bank Name**
  - **Account Name**
  - **Account Number**
- Keep all existing escaping and null-handling logic:
  - Continue using `escapeHtml(...)` for dynamic values.
  - Preserve existing `null` / blank checks and `"N/A"` fallbacks.

Apply this to:

- `buildBankDetailsSection(...)` (image version)
- `buildBankDetailsSectionForHtmlOnly(...)` (HTML-only version)

---

### 4. General constraints

- **Do not** change:
  - Feign client calls.
  - Email deduplication logic and `sentEmails` map.
  - Logging structure and log levels.
  - How `includeBankDetails` is decided/passed.
- **Do not** modify user email content except where strictly necessary to keep its original look.
- Keep the HTML structure valid (matching tags, nested `<table>` layout) for both:
  - `buildEmailBodyWithImage(...)`
  - `buildEmailBodyHtmlOnly(...)`
- Keep the admin email address coming from:
  - `reimbursement.notification.admin.email` in `services/shared-reimbursement-service/src/main/resources/application.properties`.

---

### 5. Tasks to perform

1. Inspect `ReimbursementNotificationService` and confirm the current HTML matches all requirements above.
2. If something differs, update only the relevant HTML string-building sections:
   - Control-code area in `buildEmailBodyWithImage`.
   - Control-code area in `buildEmailBodyHtmlOnly`.
   - Bank details sections in both helper methods.
3. When proposing changes, show me only the updated snippets, not the entire file.
4. Finally, explain briefly how admin and user emails will now differ in terms of:
   - Control code display.
   - Bank details display.

