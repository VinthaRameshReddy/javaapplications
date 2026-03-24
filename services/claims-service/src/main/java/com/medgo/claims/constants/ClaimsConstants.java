package com.medgo.claims.constants;

public final class ClaimsConstants {

    private ClaimsConstants() {
        throw new UnsupportedOperationException("Utility class");
    }

    public static final String SUCCESS_CODE = "000";
    public static final String ERROR_CODE = "001";
    public static final String SUCCESS_RESPONSE = "SUCCESS";
    public static final String ERROR_RESPONSE = "ERROR";
    public static final String API_BASE_PATH = "/claims-service";
    public static final String HOSPITALS_LIST_ENDPOINT = "/hospitals";
}
