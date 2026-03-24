package com.medgo.crypto.controller;

import com.medgo.crypto.dto.EncryptedWrapper;
import com.medgo.crypto.service.EncryptionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequiredArgsConstructor
public class EncryptionController {
    private final EncryptionService encryptionService;

    // Test payload encryption
    @PostMapping(value = "/encrypt", consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public EncryptedWrapper encrypt(@RequestBody Map<String, Object> plainPayload) throws Exception {
        return encryptionService.encryptPayload(plainPayload);
    }

    // Test encrypted wrapper decryption
    @PostMapping(value = "/decrypt", consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public Map decrypt(@RequestBody EncryptedWrapper encryptedRequest) throws Exception {
        return encryptionService.decryptPayload(encryptedRequest, Map.class);

    }
}

