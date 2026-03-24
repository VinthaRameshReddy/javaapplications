package com.medgo.auth.domain.response;

public record TrustDeviceResponse(
        boolean trusted,
        String message) {
}
