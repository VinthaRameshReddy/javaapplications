package com.medgo.exception;


import com.medgo.commons.CommonResponse;
import com.medgo.commons.ErrorResponse;
import com.medgo.crypto.annotation.EncryptResponse;
import com.medgo.enums.CustomStatusCode;
import io.jsonwebtoken.ExpiredJwtException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
@Component("commonExceptionHandler")
public class GlobalExceptionHandler {

    private static final Logger LOG = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    // 1. Handle database SQL exceptions
    @ExceptionHandler(ImportantHeadersMissingException.class)
    @EncryptResponse
    public CommonResponse handleMissingHeaderException(ImportantHeadersMissingException ex) {
        LOG.error("ImportantHeadersMissingException: {}", ex.getMessage());
        ErrorResponse errorResponse = new ErrorResponse(
                HttpStatus.BAD_REQUEST.value(),
                ex.getMessage()
        );

        return CommonResponse.error(errorResponse,
                HttpStatus.BAD_REQUEST.value()
        );
    }

    //  2. Handle runtime exceptions
    @ExceptionHandler(RuntimeException.class)
    @EncryptResponse
    public CommonResponse handleRuntimeException(RuntimeException ex) {
        LOG.error("RuntimeException: ", ex);
        ErrorResponse errorResponse = new ErrorResponse(
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                ex.getMessage()
        );

        return CommonResponse.error(errorResponse,
                HttpStatus.INTERNAL_SERVER_ERROR.value()
        );
    }

    // 3. Handle 500 - Unhandled exceptions
    @ExceptionHandler(Exception.class)
    @EncryptResponse
    public CommonResponse handleException(Exception ex) {
        LOG.error("Exception: ", ex);
        ErrorResponse errorResponse = new ErrorResponse(
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                ex.getMessage()
        );

        return CommonResponse.error(errorResponse,
                HttpStatus.INTERNAL_SERVER_ERROR.value()
        );
    }

    // 4. Handle 500 - expired token exceptions
    @ExceptionHandler(ExpiredJwtException.class)
    @EncryptResponse
    public CommonResponse handleException(ExpiredJwtException ex) {
        LOG.error("ExpiredJwtException: ", ex);
        ErrorResponse errorResponse = new ErrorResponse(
                CustomStatusCode.TOKEN_EXPIRED.getCode(),
                CustomStatusCode.TOKEN_EXPIRED.getMessage()
        );

        return CommonResponse.error(errorResponse,
                HttpStatus.INTERNAL_SERVER_ERROR.value()
        );
    }
}