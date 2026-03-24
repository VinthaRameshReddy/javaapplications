package com.medgo.commons;

import java.util.Map;

public class ErrorCodeCache {
    private static Map<String, String> errorCodes = null;
    public static void setErrorCodes(Map<String, String> hm) {
        errorCodes = hm;
    }
    public static String getErrorCode(String key) {
        if (errorCodes != null)
            return errorCodes.get(key);
        return null;
    }
}
