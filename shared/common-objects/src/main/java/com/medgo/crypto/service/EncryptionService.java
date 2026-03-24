package com.medgo.crypto.service;


import com.medgo.crypto.dto.EncryptedWrapper;

public interface EncryptionService {

    public EncryptedWrapper encryptPayload(Object payload) throws Exception;

    public <T> T decryptPayload(EncryptedWrapper wrapper, Class<T> valueType) throws Exception;
}
