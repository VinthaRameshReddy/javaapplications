package com.medgo.crypto.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record EncryptedWrapper(
    @JsonProperty("encryptedData")
    String encryptedData,

    @JsonProperty("encryptedKey")
    String encryptedKey,

    @JsonProperty("iv")
    String iv) {
} 