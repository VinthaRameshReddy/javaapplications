package com.medgo.jwt;


public interface TokenBlacklistStore {

    void blacklist(String token, long secondsToExpire);

    boolean isBlacklisted(String token);
}


