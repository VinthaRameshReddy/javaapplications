package com.medgo.reimburse.exception;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.http.HttpStatus;

@Data
@EqualsAndHashCode(callSuper = false)
public class ApplicationException extends RuntimeException {


    private final HttpStatus httpStatus;
    private final Object data;

    // Simple constructor
    public ApplicationException(HttpStatus httpStatus, String message) {
        super(message);  // <-- important!
        this.httpStatus = httpStatus;
        this.data = null;
    }

    // Constructor with data and cause
    public ApplicationException(HttpStatus httpStatus, String message, Object data, Throwable cause) {
        super(message, cause);  // <-- important!
        this.httpStatus = httpStatus;
        this.data = data;
    }
}
