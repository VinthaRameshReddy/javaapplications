package com.medgo.auth.service;

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

        log.info("🔐 Initializing Azure Key Vault Service for URI: {}", keyVaultUrl);

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
            secretClient.listPropertiesOfSecrets().stream().findFirst();
            log.info("✅ Connected to Azure Key Vault successfully.");
        } catch (Exception e) {
            log.error(" Failed to connect to Azure Key Vault: {}", e.getMessage());
        }
    }

    public String getSecretValue(String name) {
        return cache.computeIfAbsent(name, key -> {
            try {
                KeyVaultSecret secret = secretClient.getSecret(key);
                return secret.getValue();
            } catch (Exception e) {
                log.error("⚠️ Unable to fetch secret '{}': {}", key, e.getMessage());
                return null;
            }
        });
    }
}

