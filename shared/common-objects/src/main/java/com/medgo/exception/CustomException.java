package com.medgo.exception;

import com.medgo.commons.ErrorResponse;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class CustomException extends RuntimeException {
    private final ErrorResponse errorResponse;


    public CustomException(int statusCode, String message) {
        super(message);
        this.errorResponse = new ErrorResponse(statusCode, message);
    }
}
