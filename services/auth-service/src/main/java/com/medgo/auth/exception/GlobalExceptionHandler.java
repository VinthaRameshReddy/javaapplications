package com.medgo.auth.exception;

import com.medgo.commons.CommonResponse;
import com.medgo.commons.ErrorResponse;
import com.medgo.crypto.annotation.EncryptResponse;
import com.medgo.exception.CustomException;
import com.medgo.exception.ImportantHeadersMissingException;
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

    // 2. Handle Spring DataAccess exceptions
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

    // 3. Handle runtime exceptions
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

    // 4. Handle generic exceptions (fallback for unhandled cases)
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

    // 5. Handle custom input exceptions
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

    // 6. Handle validation errors from @Valid annotated inputs
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @EncryptResponse
    public CommonResponse handleValidationExceptions(MethodArgumentNotValidException ex) {
        List<String> validationFailures = new ArrayList<>();

        ex.getBindingResult()
          .getFieldErrors()
          .forEach(error -> validationFailures.add(error.getDefaultMessage())
          );

        // Join messages without brackets - if single error, return it directly; if multiple, join with comma
        String errorMessage = validationFailures.size() == 1 
                ? validationFailures.get(0) 
                : String.join(", ", validationFailures);

        return CommonResponse.error(
                new ErrorResponse(
                        HttpStatus.BAD_REQUEST.value(), errorMessage
                ),
                HttpStatus.BAD_REQUEST.value()
        );
    }

    // 7. Handle missing required headers
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

    // 8. Handle missing required headers
    @ExceptionHandler(CustomException.class)
    @EncryptResponse
    public CommonResponse handleCustomException(CustomException ex) {
        return CommonResponse.error(
                ex.getErrorResponse(),
                HttpStatus.BAD_REQUEST.value()
        );
    }



}
