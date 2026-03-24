package com.medgo.reimburse.domain.response;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Collections;
import java.util.List;

public record ErrorResponse(int statusCode, String errorCode, String message, List<String> errors) {

    @JsonCreator
    public ErrorResponse(
            @JsonProperty("statusCode") int statusCode,
            @JsonProperty("errorCode") String errorCode,
            @JsonProperty("message") String message,
            @JsonProperty("errors") List<String> errors) {
        this.statusCode = statusCode;
        this.errorCode = errorCode;
        this.message = message;
        this.errors = errors;
    }

    public ErrorResponse(int statusCode,
                         String message) {
        this(statusCode, null, message, Collections.emptyList());
    }

    private ErrorResponse(int statusCode, String message,
                         List<String> errors) {
        this(statusCode, null, message, errors);
    }

    public ErrorResponse(int statusCode, String errorCode,
                         String message) {
        this(statusCode, errorCode, message, Collections.emptyList());
    }
}