# Feign Client 403 Forbidden - Key Vault Mismatch

## 🔍 Problem

Getting `403 Forbidden` (or `400 Bad Request`) when calling shared-membership-service, even though credentials are configured.

## 🎯 Root Cause: Different Key Vaults

**Membership Service Key Vault:**
- URI: `https://kv-mp01-sea-a-med2g01.vault.azure.net/`
- Client ID: `4f0831c8-40bc-4de5-9650-faa550a9fc65`

**Shared Service Key Vault:**
- URI: `https://kv-mpi-sea-a-shared01.vault.azure.net/`
- Client ID: `83fbc0fc-0500-4bec-9098-4b7b5cf928fb`

### The Problem:

1. **Different Key Vaults**: Membership service and shared service use **different Azure Key Vaults**
2. **Different Secrets**: Secrets might not exist in membership service's Key Vault, or have different values
3. **Fallback to application.properties**: Membership service falls back to `application.properties` values
4. **Credential Mismatch**: If shared service loads different values from its Key Vault, credentials won't match

## 📋 Credential Flow

### Shared Service (shared-membership-service):
1. Loads from Key Vault: `kv-mpi-sea-a-shared01`
   - `app-security-user`
   - `app-security-password-bcrypt` (bcrypt hash)
   - `app-security-api-key-header`
   - `app-security-api-key`
2. Uses these values to validate:
   - Basic Auth (username/password)
   - API Key header and value

### Membership Service (membership-service):
1. Tries to load from Key Vault: `kv-mp01-sea-a-med2g01`
   - `app-security-user` (might not exist)
   - `app-security-password` (might not exist - only bcrypt exists)
   - `app-security-api-key-header` (might not exist)
   - `app-security-api-key` (might not exist)
2. Falls back to `application.properties`:
   - `shared.membership.basic.user=internal-client`
   - `shared.membership.basic.password=secret`
   - `shared.membership.apim.header=Ocp-Apim-Subscription-Key`
   - `shared.membership.apim.key=62670b5bf89b41038ff0284eea190b0c`

## ✅ Solution Options

### Option 1: Use Same Key Vault (Recommended)

Update membership service to use the same Key Vault as shared service:

```properties
# In membership-service/application.properties
azure.keyvault.uri=https://kv-mpi-sea-a-shared01.vault.azure.net/
azure.client-id=83fbc0fc-0500-4bec-9098-4b7b5cf928fb
azure.client-secret=<same-secret-as-shared-service>
azure.tenant-id=31e62360-d307-45a7-932a-f774aa7a6288
```

**Pros:**
- Both services use same credentials
- Single source of truth
- Easier to manage

**Cons:**
- Requires access to shared service's Key Vault
- Might require permission changes

### Option 2: Copy Secrets to Membership Service Key Vault

Copy the required secrets from shared service Key Vault to membership service Key Vault:

**Required Secrets:**
- `app-security-user` = `internal-client`
- `app-security-password` = `secret` (plain password for Basic Auth)
- `app-security-api-key-header` = `Ocp-Apim-Subscription-Key`
- `app-security-api-key` = `62670b5bf89b41038ff0284eea190b0c`

**Pros:**
- Services can use their own Key Vaults
- Better separation of concerns

**Cons:**
- Secrets need to be kept in sync
- More maintenance

### Option 3: Use application.properties Fallback (Current)

Ensure `application.properties` values match what shared service expects:

```properties
# These should match the commented values in shared service
shared.membership.basic.user=internal-client
shared.membership.basic.password=secret
shared.membership.apim.header=Ocp-Apim-Subscription-Key
shared.membership.apim.key=62670b5bf89b41038ff0284eea190b0c
```

**Pros:**
- Simple, no Key Vault changes needed
- Works if shared service also uses fallback values

**Cons:**
- Only works if shared service uses same fallback values
- If shared service loads from Key Vault with different values, won't work

## 🔍 Current Status

### What We Know:
1. ✅ `userId` header is being removed correctly
2. ✅ Basic Auth and API Key headers are being added
3. ❌ Credentials might not match what shared service expects
4. ❌ Different Key Vaults might have different secret values

### What to Check:

1. **Check Startup Logs:**
   ```
   ✅ FeignClientConfig initialized:
      Basic Auth User: internal-client
      Basic Auth Password: ***
      APIM Header: Ocp-Apim-Subscription-Key
      APIM Key: 62670b5b***
   ```

2. **Check Error Logs:**
   ```
   ❌❌❌ Feign Error Response:
      Status Code: 401 (or 403)
      Response Body: (check Feign FULL logs)
   ```

3. **Verify Credentials Match:**
   - Check if shared service is using Key Vault or fallback values
   - If Key Vault, verify the values match
   - If fallback, verify application.properties values match

## 🎯 Next Steps

1. **Check Error Response**: The error decoder will now show the actual status code and response
2. **Verify Credentials**: Check startup logs to see which credentials are being used
3. **Compare Key Vaults**: Verify if secrets exist in membership service's Key Vault
4. **Test with Matching Credentials**: Ensure credentials match what shared service expects

## 📝 Notes

- The shared service stores password as **bcrypt hash** for Spring Security
- But for Basic Auth, we need the **plain password** that matches the bcrypt hash
- The bcrypt hash `{bcrypt}$2b$10$LffsYo12lFmLu72LHNXy3.bTsQDcJhFbbIQ7Q4brkLOyVQOUIjQ7W` is for password `secret`
- So `internal-client` / `secret` should work if shared service also uses these values

---

**🔍 Check the error decoder logs to see the actual status code and response body!**



