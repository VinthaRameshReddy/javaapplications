# Azure Key Vault Integration (MEDGO 2.0)

This document explains how Azure Key Vault is integrated in[appointment-service](../../services/appointment-service) the services of this
repository, the runtime flow, and how key rotation impacts the running services.

## Where it is used

Azure Key Vault access is implemented in the following services (each contains
an `AzureKeyVaultService` implementation and configuration that consumes it):

- `services/auth-service`
- `services/claims-service`
- `services/membership-service`
- `services/notification-service`
- `services/provider-service`
- `services/shared-membership-service`
- `services/shared-notification-service`
- `services/shared-provider-service`
- `services/shared-reimbursement-service`
- `services/shared-utilization-service`
- `services/shared-virtualid-service`

## Components involved

- **`AzureKeyVaultService`**
  - Builds a `ClientSecretCredential` using:
    - `azure.client-id`
    - `azure.client-secret`
    - `azure.tenant-id`
  - Builds a `SecretClient` for the vault URL:
    - `azure.keyvault.uri`
  - On startup, verifies connectivity via `listPropertiesOfSecrets()`.
  - Caches secret values in an in-memory `ConcurrentHashMap`.
- **Consumers**
  - `SecurityConfig`, `FeignClientConfig`, and similar config classes call
    `getSecretValue()` to read secrets (API keys, basic auth credentials, etc.).

## Configuration values

These values are expected in service configuration (for example,
`application.properties` or environment variables):

- `azure.keyvault.uri`
- `azure.client-id`
- `azure.client-secret`
- `azure.tenant-id`

## Flow diagram (startup + secret retrieval)

```mermaid
flowchart TD
    A[Service starts] --> B[Spring creates AzureKeyVaultService]
    B --> C[Build ClientSecretCredential]
    C --> D[Build SecretClient with vault URL]
    D --> E[@PostConstruct verify connection]
    E --> F[Config beans request secrets]
    F --> G{Secret cached?}
    G -->|Yes| H[Return cached value]
    G -->|No| I[secretClient.getSecret(name)]
    I --> J[Cache value in memory]
    J --> H
```

## Steps involved (current implementation)

1. Service starts and Spring instantiates `AzureKeyVaultService`.
2. The service creates a `ClientSecretCredential` from the configured Azure
   client ID, client secret, and tenant ID.
3. A `SecretClient` is created with the Key Vault URI.
4. `@PostConstruct` performs a connectivity check.
5. Configuration classes (security, Feign clients, etc.) call
   `getSecretValue("secret-name")`.
6. `getSecretValue` returns a cached value if present. Otherwise, it calls
   Azure Key Vault, stores the value in memory, and returns it.

## Key rotation behavior (current)

The existing implementation caches secrets in memory without a TTL. That means:

- A secret is fetched once per service instance and then reused.
- When a secret is rotated in Azure Key Vault, the **running service does not
  automatically refetch** the new value.
- A redeploy/restart clears the cache and forces a fresh fetch.
- If a downstream system rejects the old secret (expired/disabled), requests
  will fail until the service is restarted.

## Key rotation flow (operational steps)

Use this flow when rotating secrets in Azure Key Vault:

1. **Create a new version** of the secret in Key Vault.
2. **Keep the previous version valid** for a short overlap window (if possible).
3. **Rolling restart/redeploy** the affected services so the in-memory cache is
   cleared and the new secret is fetched.
4. **Validate** by calling an endpoint that uses the rotated secret.
5. **Disable/expire the old version** after validation.

## Optional improvement ideas (if rotation must be automatic)

If you need services to pick up rotated secrets without a restart, add one of
these patterns:

- **TTL cache**: expire entries after N minutes, then refetch.
- **Scheduled refresh**: a scheduled job that clears the cache or refetches
  secrets periodically.
- **Refresh on failure**: if a downstream auth call fails, retry once after
  reloading the secret from Key Vault.

