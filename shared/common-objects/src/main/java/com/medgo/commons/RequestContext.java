package com.medgo.commons;

public class RequestContext {
    private static final ThreadLocal<String> traceIdHolder = new ThreadLocal<>();
    private static final ThreadLocal<String> deviceIdHolder = new ThreadLocal<>();
    private static final ThreadLocal<String> userNameHolder = new ThreadLocal<>();
    private static final ThreadLocal<String> userIdHolder = new ThreadLocal<>();



    public static void setTraceId(String traceId) {
        traceIdHolder.set(traceId);
    }

    public static String getTraceId() {
        return traceIdHolder.get();
    }

    public static void clear() {
        traceIdHolder.remove();
        deviceIdHolder.remove();
        userNameHolder.remove();
        userIdHolder.remove();
    }

    public static void setDeviceId(String deviceId) {
        deviceIdHolder.set(deviceId);
    }

    public static String getDeviceId() {
        return deviceIdHolder.get();
    }

    public static void setUserName(String userName) {
        userNameHolder.set(userName);
    }

    public static String getUserName() {
        return userNameHolder.get();
    }

    public static void setUserId(String userId) {
        userIdHolder.set(userId);
    }

    public static String getUserId() {
        return userIdHolder.get();
    }
}
