package com.medgo.exception;

public class ImportantHeadersMissingException extends RuntimeException {
    public ImportantHeadersMissingException(String message) {
        super(message);
    }
}
