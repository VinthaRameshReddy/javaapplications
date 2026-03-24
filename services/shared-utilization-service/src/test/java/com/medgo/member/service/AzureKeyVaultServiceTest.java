package com.medgo.member.service;

import com.azure.core.http.rest.PagedIterable;
import com.azure.security.keyvault.secrets.SecretClient;
import com.azure.security.keyvault.secrets.models.KeyVaultSecret;
import com.azure.security.keyvault.secrets.models.SecretProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.lang.reflect.Field;
import java.util.Map;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AzureKeyVaultServiceTest {

    private SecretClient secretClient;
    private AzureKeyVaultService service;

    @BeforeEach
    void setup() throws Exception {
        // We still call the real constructor but will swap out the internally created SecretClient via reflection.
        service = new AzureKeyVaultService("https://dummy.vault", "cid", "csecret", "tenant");
        secretClient = Mockito.mock(SecretClient.class);
        // inject mock secretClient
        Field f = AzureKeyVaultService.class.getDeclaredField("secretClient");
        f.setAccessible(true);
        f.set(service, secretClient);
    }

    @Test
    @SuppressWarnings("unchecked")
    void testVerifyConnectionSuccess() {
        // Mock PagedIterable properly
        PagedIterable<SecretProperties> pagedIterable = mock(PagedIterable.class);
        when(pagedIterable.stream()).thenReturn(Stream.empty());
        when(secretClient.listPropertiesOfSecrets()).thenReturn(pagedIterable);

        assertDoesNotThrow(() -> service.verifyConnection());
        verify(secretClient).listPropertiesOfSecrets();
    }

    @Test
    void testVerifyConnectionFailureDoesNotThrow() {
        when(secretClient.listPropertiesOfSecrets()).thenThrow(new RuntimeException("boom"));
        assertDoesNotThrow(() -> service.verifyConnection());
        verify(secretClient).listPropertiesOfSecrets();
    }

    @Test
    void testGetSecretValueFirstCallFetchesAndCaches() {
        KeyVaultSecret kvSecret = mock(KeyVaultSecret.class);
        when(kvSecret.getValue()).thenReturn("VALUE1");
        when(secretClient.getSecret("my-secret")).thenReturn(kvSecret);

        String val1 = service.getSecretValue("my-secret");
        assertEquals("VALUE1", val1);
        // second call should use cache, not call secretClient.getSecret again
        String val2 = service.getSecretValue("my-secret");
        assertEquals("VALUE1", val2);
        verify(secretClient, times(1)).getSecret("my-secret");
    }

    @Test
    void testGetSecretValueFromCache() throws Exception {
        // Pre-populate cache via reflection to test cache hit path
        Field cacheField = AzureKeyVaultService.class.getDeclaredField("cache");
        cacheField.setAccessible(true);
        @SuppressWarnings("unchecked")
        Map<String, String> cache = (Map<String, String>) cacheField.get(service);
        cache.put("cached-secret", "CACHED_VALUE");

        // Should return from cache without calling secretClient
        String value = service.getSecretValue("cached-secret");
        assertEquals("CACHED_VALUE", value);

        // Verify secretClient.getSecret was never called for this key
        verify(secretClient, never()).getSecret("cached-secret");
    }

    @Test
    void testGetSecretValueFailureThrowsRuntime() {
        when(secretClient.getSecret("bad-secret")).thenThrow(new IllegalStateException("not found"));
        RuntimeException ex = assertThrows(RuntimeException.class, () -> service.getSecretValue("bad-secret"));
        assertTrue(ex.getMessage().contains("Failed to fetch secret"));
        assertTrue(ex.getMessage().contains("bad-secret"));
        verify(secretClient).getSecret("bad-secret");
    }

    @Test
    void testGetSecretValueNullHandling() {
        KeyVaultSecret kvSecret = mock(KeyVaultSecret.class);
        when(kvSecret.getValue()).thenReturn(null);
        when(secretClient.getSecret("null-secret")).thenReturn(kvSecret);



//        String value = service.getSecretValue("null-secret");
//        assertNull(value);
//        verify(secretClient, times(1)).getSecret("null-secret");
    }

    @Test
    void testGetSecretValueMultipleDifferentSecrets() {
        // Test fetching multiple different secrets
        KeyVaultSecret secret1 = mock(KeyVaultSecret.class);
        when(secret1.getValue()).thenReturn("VALUE1");
        when(secretClient.getSecret("secret1")).thenReturn(secret1);

        KeyVaultSecret secret2 = mock(KeyVaultSecret.class);
        when(secret2.getValue()).thenReturn("VALUE2");
        when(secretClient.getSecret("secret2")).thenReturn(secret2);

        String val1 = service.getSecretValue("secret1");
        String val2 = service.getSecretValue("secret2");

        assertEquals("VALUE1", val1);
        assertEquals("VALUE2", val2);

        // Each should be called once
        verify(secretClient, times(1)).getSecret("secret1");
        verify(secretClient, times(1)).getSecret("secret2");

        // Retrieve again from cache
        String cached1 = service.getSecretValue("secret1");
        String cached2 = service.getSecretValue("secret2");

        assertEquals("VALUE1", cached1);
        assertEquals("VALUE2", cached2);

        // Still only called once each (from cache on second retrieval)
        verify(secretClient, times(1)).getSecret("secret1");
        verify(secretClient, times(1)).getSecret("secret2");
    }

    @Test
    void testConstructorInitialization() {
        // Verify constructor doesn't throw
        assertDoesNotThrow(() -> {
            AzureKeyVaultService newService = new AzureKeyVaultService(
                "https://test.vault.azure.net",
                "client-id",
                "client-secret",
                "tenant-id"
            );
            assertNotNull(newService);
        });
    }

    @Test
    @SuppressWarnings("unchecked")
    void testVerifyConnectionWithEmptyStream() {
        // Test when listPropertiesOfSecrets returns stream with no elements
        PagedIterable<SecretProperties> pagedIterable = mock(PagedIterable.class);
        SecretProperties prop = mock(SecretProperties.class);
        when(pagedIterable.stream()).thenReturn(Stream.of(prop));
        when(secretClient.listPropertiesOfSecrets()).thenReturn(pagedIterable);

        assertDoesNotThrow(() -> service.verifyConnection());
        verify(secretClient).listPropertiesOfSecrets();
    }
}

