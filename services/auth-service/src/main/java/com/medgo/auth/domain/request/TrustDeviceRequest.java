package com.medgo.auth.domain.request;

import com.fasterxml.jackson.annotation.JsonProperty;

public record TrustDeviceRequest(

        @JsonProperty("userId")
        String userId,

        @JsonProperty("email")
        String email,

        @JsonProperty("deviceId")
        String deviceId,

        @JsonProperty("platform")
        String platform,

        @JsonProperty("deviceBrand")
        String deviceBrand,

        @JsonProperty("deviceModel")
        String deviceModel,

        @JsonProperty("deviceOs")
        String deviceOs,

        @JsonProperty("location")
        String location) {
}
