# Prompt: Reimplement BankAccountValidationService

**Context:** shared-reimbursement-service. Reimbursement submission (initial and resubmit) validates bank name + account number before processing. We need a dedicated service for Philippine bank account digit-length rules.

**Deliverable:** One class: `com.medgo.reimburse.service.BankAccountValidationService`.

---

## Requirements

1. **Package / class**
   - Package: `com.medgo.reimburse.service`
   - Class: `BankAccountValidationService`
   - Annotate with `@Service` (Spring) and `@Slf4j` (Lombok). No interface; concrete class only.

2. **Core validation**
   - Method: `void validateBankAccount(String bankName, String bankAccountNumber)`.
   - If `bankName` is null or blank: log a warning and return (do not throw). Skip validation.
   - If `bankAccountNumber` is null or blank: throw `IllegalArgumentException("Bank account number is required")`.
   - Resolve rules by bank name (see below). If no rule is found for the bank: log warning and return (skip validation).
   - Normalize account number by removing all non-digit characters (spaces, dashes, etc.). If nothing remains: throw `IllegalArgumentException("Bank account number must contain at least one digit")`.
   - Check digit count against the rule's min/max. If out of range: throw `IllegalArgumentException` with a clear message including bank name, required range, and actual length. Use a single message format when min == max (e.g. "Required: X digits") and a range format when min != max (e.g. "Required: X to Y digits"). Log the failure at ERROR before throwing.
   - On success, log at DEBUG (bank name, digit count, required range).

3. **Bank rules storage**
   - Use a static `Map<String, int[]>` keyed by normalized bank name (lowercase). Value is `{ minDigits, maxDigits }`.
   - Keys must be lowercase. Support multiple keys per bank (e.g. "bdo", "banco de oro") pointing to the same `int[]`.
   - Populate the map in a static block with the following Philippine banks and digit rules (exact as below):

   | Bank (key variants) | minDigits | maxDigits |
   |--------------------|-----------|-----------|
   | bdo, banco de oro | 12 | 12 |
   | bpi, bank of the philippine islands | 10 | 10 |
   | chinabank, china bank | 10 | 12 |
   | eastwest, eastwest bank, east west | 12 | 12 |
   | lpb, land bank, land bank of the philippines | 10 | 10 |
   | metrobank, metro bank | 13 | 13 |
   | pnb, philippine national bank | 12 | 12 |
   | rcbc, rizal commercial banking corporation | 10 | 10 |
   | security bank | 13 | 13 |
   | ucpb, united coco planters bank | 12 | 12 |
   | ubp, union bank, union bank of the philippines | 12 | 12 |
   | phil. business bank, phil business bank, philippine business bank, pbcom | 12 | 12 |

4. **Lookup logic**
   - Normalize input bank name: `trim().toLowerCase()`.
   - First try exact key match in the map. If found, use that `int[]`.
   - If not found, use partial match: iterate map entries and return the first where `normalizedBankName.contains(key)` OR `key.contains(normalizedBankName)`. If no match, return null (caller will skip validation).

5. **Helper methods (public API)**
   - `String getRequiredDigitsInfo(String bankName)`: return null if bank name null/blank or bank unknown; otherwise return a user-friendly string: single number when min == max (e.g. "12"), or "min to max" when different (e.g. "10 to 12").
   - `Integer getMaxLength(String bankName)`: return null if bank name null/blank or bank unknown; otherwise return the max digit count (second element of the rule array).
   - `Integer getMinLength(String bankName)`: return null if bank name null/blank or bank unknown; otherwise return the min digit count (first element of the rule array).

6. **Dependencies**
   - Only standard Java and project libs: `java.util` (Map, HashMap), Lombok `@Slf4j`, Spring `@Service`. No other services or repositories.

7. **Usage**
   - Injected in `ReimbursementsServiceImpl` and used in both submission flows. Call site: `bankAccountValidationService.validateBankAccount(request.getBankName(), request.getBankAccountNumber())`. Ensure the new class lives in `service` package so the existing constructor injection and imports continue to work.

---

## Acceptance

- Unknown or missing bank name never throws; it skips validation with a warning.
- Missing or invalid account number (null, blank, or no digits) throws `IllegalArgumentException` with a clear message.
- Wrong digit count for a known bank throws `IllegalArgumentException` with bank name and digit requirements in the message.
- All listed banks and key variants are in the static map with correct min/max.
- `getRequiredDigitsInfo`, `getMaxLength`, and `getMinLength` behave as specified and return null when bank is unknown or name is null/blank.
