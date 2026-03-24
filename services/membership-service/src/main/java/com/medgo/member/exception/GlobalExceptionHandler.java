package com.medgo.member.exception;

import com.medgo.commons.CommonResponse;
import com.medgo.commons.ErrorResponse;
import com.medgo.crypto.annotation.EncryptResponse;
import feign.FeignException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger LOG = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /**
     * Handle MemberCode validation exceptions
     */
    @ExceptionHandler(MemberCodeValidationException.class)
    @EncryptResponse
    public CommonResponse handleMemberCodeValidationException(MemberCodeValidationException ex) {
        LOG.error("SECURITY ALERT: MemberCode validation failed for memberCode: {}", ex.getMemberCode());
        ErrorResponse errorResponse = new ErrorResponse(
                HttpStatus.FORBIDDEN.value(),
                ex.getMessage() != null ? ex.getMessage() : "MemberCode does not belong to authenticated user. Access denied."
        );
        return CommonResponse.error(errorResponse, HttpStatus.FORBIDDEN.value());
    }

    /**
     * Handle Feign 404 Not Found exceptions
     */
    @ExceptionHandler(feign.FeignException.NotFound.class)
    @EncryptResponse
    public CommonResponse handleFeignNotFoundException(FeignException.NotFound ex) {
        LOG.warn("Feign 404 Not Found: {}", ex.getMessage());
        ErrorResponse errorResponse = new ErrorResponse(
                HttpStatus.NOT_FOUND.value(),
                "Resource not found"
        );
        return CommonResponse.error(errorResponse, HttpStatus.NOT_FOUND.value());
    }

    /**
     * Handle other Feign exceptions (4xx, 5xx)
     */
    @ExceptionHandler(FeignException.class)
    @EncryptResponse
    public CommonResponse handleFeignException(FeignException ex) {
        LOG.error("Feign Exception: status={}, message={}", ex.status(), ex.getMessage());
        ErrorResponse errorResponse = new ErrorResponse(
                ex.status(),
                ex.getMessage() != null ? ex.getMessage() : "External service error"
        );
        return CommonResponse.error(errorResponse, ex.status());
    }

    /**
     * Handle runtime exceptions
     */
    @ExceptionHandler(RuntimeException.class)
    @EncryptResponse
    public CommonResponse handleRuntimeException(RuntimeException ex) {
        // Skip MemberCodeValidationException as it's handled separately
        if (ex instanceof MemberCodeValidationException) {
            return handleMemberCodeValidationException((MemberCodeValidationException) ex);
        }
        LOG.error("RuntimeException: ", ex);
        ErrorResponse errorResponse = new ErrorResponse(
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                ex.getMessage() != null ? ex.getMessage() : "Internal server error"
        );
        return CommonResponse.error(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR.value());
    }

    /**
     * Handle generic exceptions (fallback)
     */
    @ExceptionHandler(Exception.class)
    @EncryptResponse
    public CommonResponse handleException(Exception ex) {
        LOG.error("Exception: ", ex);
        ErrorResponse errorResponse = new ErrorResponse(
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                ex.getMessage() != null ? ex.getMessage() : "An unexpected error occurred"
        );
        return CommonResponse.error(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR.value());
    }
}

