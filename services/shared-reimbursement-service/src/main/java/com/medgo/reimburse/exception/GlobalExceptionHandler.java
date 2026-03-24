package com.medgo.reimburse.exception;


import com.medgo.commons.CommonResponse;
import com.medgo.commons.ErrorResponse;
import com.medgo.crypto.annotation.EncryptResponse;
import com.medgo.exception.CustomException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

@RestControllerAdvice
@Component("authExceptionHandler")
public class GlobalExceptionHandler {

    private static final Logger LOG = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    //  1. Handle database SQL exceptions
    // 1. Handle database SQL exceptions
    @ExceptionHandler(SQLException.class)
    @EncryptResponse
    public CommonResponse handleSQLException(SQLException ex) {
        LOG.error("SQL Exception: ", ex);

        ErrorResponse errorResponse = new ErrorResponse(
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                ex.getMessage()
        );

        return CommonResponse.error(
                errorResponse,
                HttpStatus.INTERNAL_SERVER_ERROR.value()
        );
    }


    //  2. Handle Spring DataAccess exceptions
    @ExceptionHandler(DataAccessException.class)
    @EncryptResponse
    public CommonResponse handleDataAccessException(DataAccessException ex) {
        LOG.error("DataAccessException: ", ex);
        ErrorResponse errorResponse = new ErrorResponse(
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                ex.getMostSpecificCause().getMessage()
        );
        return CommonResponse.error(errorResponse,
                                    HttpStatus.INTERNAL_SERVER_ERROR.value()
        );
    }

    //  3. Handle runtime exceptions
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

    @ExceptionHandler(CustomException.class)
    @EncryptResponse
    public CommonResponse handleCustomException(CustomException ex) {
        LOG.error("CustomException: ", ex);
        return CommonResponse.error(ex.getErrorResponse(),
                                    HttpStatus.INTERNAL_SERVER_ERROR.value()
        );
    }

    // 4. Handle 500 - Unhandled exceptions
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

    @ExceptionHandler(InputException.class)
    @EncryptResponse
    public CommonResponse handleInputException(InputException ex) {
        LOG.error("ProviderInputException: ", ex);
        ErrorResponse errorResponse = new ErrorResponse(
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                ex.getMessage()
        );

        return CommonResponse.error(errorResponse,
                                    HttpStatus.INTERNAL_SERVER_ERROR.value()
        );
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @EncryptResponse
    public CommonResponse handleValidationExceptions(MethodArgumentNotValidException ex) {
        List<String> validationFailures = new ArrayList<>();

        ex.getBindingResult()
          .getFieldErrors()
          .forEach(error -> validationFailures.add(error.getField() + ": " + error.getDefaultMessage())
          );


        return CommonResponse.error(
                new ErrorResponse(
                        HttpStatus.BAD_REQUEST.value(), validationFailures.toString()
                ),
                HttpStatus.BAD_REQUEST.value()
        );
    }

    @ExceptionHandler(MemberCodeValidationException.class)
    @EncryptResponse
    public CommonResponse handleMemberCodeValidationException(MemberCodeValidationException ex) {
        LOG.error("MemberCodeValidationException for memberCode: {}", ex.getMemberCode(), ex);
        ErrorResponse errorResponse = new ErrorResponse(
                HttpStatus.FORBIDDEN.value(),
                ex.getMessage()
        );
        return CommonResponse.error(errorResponse, HttpStatus.FORBIDDEN.value());
    }

    @ExceptionHandler(IllegalArgumentException.class)
    @EncryptResponse
    public CommonResponse handleIllegalArgumentException(IllegalArgumentException ex) {
        LOG.error("IllegalArgumentException: ", ex);
        ErrorResponse errorResponse = new ErrorResponse(
                HttpStatus.BAD_REQUEST.value(),
                ex.getMessage()
        );
        return CommonResponse.error(errorResponse, HttpStatus.BAD_REQUEST.value());
    }

    @ExceptionHandler(DualDatabaseTransactionException.class)
    @EncryptResponse
    public CommonResponse handleDualDatabaseTransactionException(DualDatabaseTransactionException ex) {
        LOG.error("DualDatabaseTransactionException - Control Code: {}, Failed Database: {}", 
                ex.getControlCode(), ex.getFailedDatabase(), ex);
        
        // Log the root cause for debugging
        if (ex.getCause() != null) {
            LOG.error("Root cause: {}", ex.getCause().getMessage(), ex.getCause());
        }
        
        ErrorResponse errorResponse = new ErrorResponse(
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                ex.getMessage()
        );
        return CommonResponse.error(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR.value());
    }

}