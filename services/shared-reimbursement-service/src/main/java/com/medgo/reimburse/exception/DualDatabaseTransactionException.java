package com.medgo.reimburse.exception;

public class DualDatabaseTransactionException extends RuntimeException {
    private final String controlCode;
    private final String failedDatabase;

    public DualDatabaseTransactionException(String message, String controlCode, String failedDatabase, Throwable cause) {
        super(message, cause);
        this.controlCode = controlCode;
        this.failedDatabase = failedDatabase;
    }

    public String getControlCode() {
        return controlCode;
    }

    public String getFailedDatabase() {
        return failedDatabase;
    }
}
