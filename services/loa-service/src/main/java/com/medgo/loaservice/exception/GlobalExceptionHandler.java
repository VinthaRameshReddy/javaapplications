package com.medgo.loaservice.exception;

import com.medgo.commons.CommonResponse;
import com.medgo.commons.ErrorResponse;
import com.medgo.crypto.annotation.EncryptResponse;
import feign.FeignException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;


@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(feign.FeignException.Conflict.class)
    @EncryptResponse
    public CommonResponse handleFeignConflictException(FeignException.Conflict ex) {
        log.warn("Feign 409 Conflict: {}", ex.getMessage());

        String errorMessage = "Business logic error";
        if (ex.contentUTF8() != null && !ex.contentUTF8().isEmpty()) {
            errorMessage = ex.contentUTF8();
        } else if (ex.getMessage() != null) {
            errorMessage = ex.getMessage();
        }

        ErrorResponse errorResponse = new ErrorResponse(
                HttpStatus.CONFLICT.value(),
                errorMessage
        );
        return CommonResponse.error(errorResponse, HttpStatus.CONFLICT.value());
    }


    @ExceptionHandler(feign.FeignException.Unauthorized.class)
    @EncryptResponse
    public CommonResponse handleFeignUnauthorizedException(FeignException.Unauthorized ex) {
        log.error("Feign 401 Unauthorized: {}", ex.getMessage());
        ErrorResponse errorResponse = new ErrorResponse(
                HttpStatus.UNAUTHORIZED.value(),
                ex.getMessage() != null ? ex.getMessage() : "Authentication failed"
        );
        return CommonResponse.error(errorResponse, HttpStatus.UNAUTHORIZED.value());
    }


    @ExceptionHandler(feign.FeignException.Forbidden.class)
    @EncryptResponse
    public CommonResponse handleFeignForbiddenException(FeignException.Forbidden ex) {
        log.error("Feign 403 Forbidden: {}", ex.getMessage());
        ErrorResponse errorResponse = new ErrorResponse(
                HttpStatus.FORBIDDEN.value(),
                ex.getMessage() != null ? ex.getMessage() : "Access denied"
        );
        return CommonResponse.error(errorResponse, HttpStatus.FORBIDDEN.value());
    }


    @ExceptionHandler(feign.FeignException.NotFound.class)
    @EncryptResponse
    public CommonResponse handleFeignNotFoundException(FeignException.NotFound ex) {
        log.warn("Feign 404 Not Found: {}", ex.getMessage());
        ErrorResponse errorResponse = new ErrorResponse(
                HttpStatus.NOT_FOUND.value(),
                ex.getMessage() != null ? ex.getMessage() : "Resource not found"
        );
        return CommonResponse.error(errorResponse, HttpStatus.NOT_FOUND.value());
    }


    @ExceptionHandler(feign.FeignException.BadRequest.class)
    @EncryptResponse
    public CommonResponse handleFeignBadRequestException(FeignException.BadRequest ex) {
        log.error("Feign 400 Bad Request: {}", ex.getMessage());
        ErrorResponse errorResponse = new ErrorResponse(
                HttpStatus.BAD_REQUEST.value(),
                ex.getMessage() != null ? ex.getMessage() : "Bad request"
        );
        return CommonResponse.error(errorResponse, HttpStatus.BAD_REQUEST.value());
    }


    @ExceptionHandler(FeignException.class)
    @EncryptResponse
    public CommonResponse handleFeignException(FeignException ex) {
        int status = ex.status();
        log.error("Feign Exception: status={}, message={}", status, ex.getMessage());

        String errorMessage = "External service error";
        if (ex.contentUTF8() != null && !ex.contentUTF8().isEmpty()) {
            errorMessage = ex.contentUTF8();
        } else if (ex.getMessage() != null) {
            errorMessage = ex.getMessage();
        }

        ErrorResponse errorResponse = new ErrorResponse(
                status > 0 ? status : HttpStatus.INTERNAL_SERVER_ERROR.value(),
                errorMessage
        );
        return CommonResponse.error(errorResponse, status > 0 ? status : HttpStatus.INTERNAL_SERVER_ERROR.value());
    }

    @ExceptionHandler(MemberCodeValidationException.class)
    @EncryptResponse
    public CommonResponse handleMemberCodeValidationException(MemberCodeValidationException ex) {
        log.error("SECURITY ALERT: MemberCode validation failed for memberCode: {}", ex.getMemberCode());
        ErrorResponse errorResponse = new ErrorResponse(
                HttpStatus.FORBIDDEN.value(),
                ex.getMessage() != null ? ex.getMessage() : "MemberCode does not belong to authenticated user. Access denied."
        );
        return CommonResponse.error(errorResponse, HttpStatus.FORBIDDEN.value());
    }
}
