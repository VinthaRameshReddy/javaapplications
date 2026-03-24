package com.medgo.crypto.service.serviceImpl;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.medgo.crypto.dto.EncryptedWrapper;
import com.medgo.crypto.service.EncryptionService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class EncryptionServiceImpl implements EncryptionService {
    @Value("${client.public.key}")
    private String clientPublicKey;

    @Value("${private.key}")
    private String serverPrivateKey;

    private final ObjectMapper objectMapper;


    public EncryptedWrapper encryptPayload(Object payload) throws Exception {


        SecretKey aesKey = EncryptionUtils.generateAESKey();
        byte[] iv = EncryptionUtils.generateGCMIV();
        String payloadJson = objectMapper.writeValueAsString(payload);
        String encryptedPayload = EncryptionUtils.encryptAESGCM(payloadJson, aesKey, iv);


        PublicKey publicKey = EncryptionUtils.getPublicKey(clientPublicKey);
        String encryptedAesKey = EncryptionUtils.encryptRSA(aesKey.getEncoded(), publicKey);
        EncryptedWrapper wrapper = new EncryptedWrapper(encryptedPayload, encryptedAesKey,
                                                        Base64.getEncoder().encodeToString(iv)
        );

        return wrapper;
    }

    public <T> T decryptPayload(EncryptedWrapper wrapper, Class<T> valueType) throws Exception {
        PrivateKey privateKey = EncryptionUtils.getPrivateKey(serverPrivateKey);
        byte[] decryptedAesKeyBytes = EncryptionUtils.decryptRSA(wrapper.encryptedKey(), privateKey);
        SecretKey aesKey = new SecretKeySpec(decryptedAesKeyBytes, "AES");
        byte[] iv = Base64.getDecoder().decode(wrapper.iv());
        String decryptedPayload = EncryptionUtils.decryptAESGCM(wrapper.encryptedData(), aesKey, iv);

        // If caller expects String, just return as is
        if (valueType == String.class) {
            return (T) decryptedPayload;
        }

        // Try parsing as JSON
        try {
            return objectMapper.readValue(decryptedPayload, valueType);
        } catch (Exception e) {
            // fallback: if valueType is Map but payload is plain string, wrap it
            if (Map.class.isAssignableFrom(valueType)) {
                Map<String, Object> wrapperMap = new LinkedHashMap<>();
                wrapperMap.put("message", decryptedPayload);
                return (T) wrapperMap;
            }
            throw e;
        }
    }

}
