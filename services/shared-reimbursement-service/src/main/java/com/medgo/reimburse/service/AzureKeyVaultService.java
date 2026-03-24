package com.medgo.reimburse.service;

import com.azure.identity.ClientSecretCredential;
import com.azure.identity.ClientSecretCredentialBuilder;
import com.azure.security.keyvault.secrets.SecretClient;
import com.azure.security.keyvault.secrets.SecretClientBuilder;
import com.azure.security.keyvault.secrets.models.KeyVaultSecret;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Centralized Azure Key Vault integration service.
 * Retrieves secrets securely and caches them for performance.
 */
@Slf4j
@Service
public class AzureKeyVaultService {

    private final SecretClient secretClient;
    private final Map<String, String> cache = new ConcurrentHashMap<>();

    public AzureKeyVaultService(
            @Value("${azure.keyvault.uri}") String keyVaultUrl,
            @Value("${azure.client-id}") String clientId,
            @Value("${azure.client-secret}") String clientSecret,
            @Value("${azure.tenant-id}") String tenantId) {

        log.info("Initializing AzureKeyVaultService for vault: {}", keyVaultUrl);

        ClientSecretCredential credential = new ClientSecretCredentialBuilder()
                .clientId(clientId)
                .clientSecret(clientSecret)
                .tenantId(tenantId)
                .build();

        this.secretClient = new SecretClientBuilder()
                .vaultUrl(keyVaultUrl)
                .credential(credential)
                .buildClient();
    }

    @PostConstruct
    public void verifyConnection() {
        try {
            log.info("Verifying Azure Key Vault connectivity...");
            // ✅ Correct method name:
            secretClient.listPropertiesOfSecrets().stream().findFirst();
            log.info("✅ Successfully connected to Azure Key Vault.");
        } catch (Exception e) {
            log.error("❌ Unable to connect to Azure Key Vault: {}", e.getMessage());
        }
    }

    /**
     * Retrieve a secret value from Azure Key Vault.
     * Cached for performance and reduced latency.
     */
    public String getSecretValue(String secretName) {
        if (cache.containsKey(secretName)) {
            return cache.get(secretName);
        }

        try {
            KeyVaultSecret secret = secretClient.getSecret(secretName);
            String value = secret.getValue();
            cache.put(secretName, value);
            log.debug("Fetched secret '{}' from Azure Key Vault.", secretName);
            return value;
        } catch (Exception e) {
            log.error("❌ Error fetching secret '{}' from Azure Key Vault: {}", secretName, e.getMessage());
            throw new RuntimeException("Failed to fetch secret '" + secretName + "' from Azure Key Vault.", e);
        }
    }
}
