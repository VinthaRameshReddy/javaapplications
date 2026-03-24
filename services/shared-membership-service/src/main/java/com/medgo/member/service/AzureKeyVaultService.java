package com.medgo.member.service;

import com.azure.identity.ClientSecretCredentialBuilder;
import com.azure.security.keyvault.secrets.SecretClient;
import com.azure.security.keyvault.secrets.SecretClientBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class AzureKeyVaultService {

    private final SecretClient secretClient;

    public AzureKeyVaultService(
        @Value("${azure.keyvault.uri}") String keyVaultUrl,
        @Value("${azure.client-id}") String clientId,
        @Value("${azure.client-secret}") String clientSecret,
        @Value("${azure.tenant-id}") String tenantId) {

        this.secretClient = new SecretClientBuilder()
                .vaultUrl(keyVaultUrl)
                .credential(new ClientSecretCredentialBuilder()
                        .clientId(clientId)
                        .clientSecret(clientSecret)
                        .tenantId(tenantId)
                        .build())
                .buildClient();
    }

    public String getSecretValue(String secretName) {
        return secretClient.getSecret(secretName).getValue();
    }


}
 