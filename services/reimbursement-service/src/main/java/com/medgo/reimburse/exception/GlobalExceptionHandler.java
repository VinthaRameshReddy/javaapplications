package com.medgo.reimburse.exception;



//import com.medgo.crypto.annotation.EncryptResponse;

import com.medgo.reimburse.domain.response.CommonResponse;
import com.medgo.reimburse.domain.response.ErrorResponse;
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
   // @EncryptResponse
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








    // 4. Handle 500 - Unhandled exceptions
    @ExceptionHandler(Exception.class)
    //@EncryptResponse
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
   // @EncryptResponse
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
   // @EncryptResponse
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

}