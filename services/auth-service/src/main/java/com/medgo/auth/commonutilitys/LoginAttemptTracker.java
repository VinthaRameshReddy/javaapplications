package com.medgo.auth.commonutilitys;

import com.medgo.auth.domain.entity.medigo.NonMemberUserModel;
import com.medgo.auth.domain.entity.medigo.UserModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;

public class LoginAttemptTracker {

    private static final Logger LOGGER = LoggerFactory.getLogger(LoginAttemptTracker.class);
    private static final int MAX_LOGIN_ATTEMPTS = 5;
    private static final int LOCK_DURATION_MINUTES = 15;

    public static boolean isAccountLocked(UserModel user) {
        if (user.getAccountLockedUntil() == null) {
            return false;
        }

        if (LocalDateTime.now().isBefore(user.getAccountLockedUntil())) {
            LOGGER.warn("Account locked for user: {} until {}", user.getEmail(), user.getAccountLockedUntil());
            return true;
        }

        // Lock expired, reset the lock
        user.setAccountLockedUntil(null);
        user.setFailedLoginAttempts(0);
        LOGGER.info("Account lock expired for user: {}, resetting attempts", user.getEmail());
        return false;
    }

    public static boolean isAccountLocked(NonMemberUserModel user) {
        if (user.getAccountLockedUntil() == null) {
            return false;
        }

        if (LocalDateTime.now().isBefore(user.getAccountLockedUntil())) {
            LOGGER.warn("Account locked for non-member user: {} until {}", user.getEmail(), user.getAccountLockedUntil());
            return true;
        }

        // Lock expired, reset the lock
        user.setAccountLockedUntil(null);
        user.setFailedLoginAttempts(0);
        LOGGER.info("Account lock expired for non-member user: {}, resetting attempts", user.getEmail());
        return false;
    }

    public static void recordFailedAttempt(UserModel user) {
        Integer attempts = user.getFailedLoginAttempts();
        if (attempts == null) {
            attempts = 0;
        }
        attempts++;
        user.setFailedLoginAttempts(attempts);

        LOGGER.warn("Failed login attempt {} for user: {}", attempts, user.getEmail());

        if (attempts >= MAX_LOGIN_ATTEMPTS) {
            LocalDateTime lockUntil = LocalDateTime.now().plusMinutes(LOCK_DURATION_MINUTES);
            user.setAccountLockedUntil(lockUntil);
            LOGGER.error("Account locked for user: {} due to {} failed attempts. Locked until: {}",
                    user.getEmail(), attempts, lockUntil);
        }
    }

    public static void recordFailedAttempt(NonMemberUserModel user) {
        Integer attempts = user.getFailedLoginAttempts();
        if (attempts == null) {
            attempts = 0;
        }
        attempts++;
        user.setFailedLoginAttempts(attempts);

        LOGGER.warn("Failed login attempt {} for non-member user: {}", attempts, user.getEmail());

        if (attempts >= MAX_LOGIN_ATTEMPTS) {
            LocalDateTime lockUntil = LocalDateTime.now().plusMinutes(LOCK_DURATION_MINUTES);
            user.setAccountLockedUntil(lockUntil);
            LOGGER.error("Account locked for non-member user: {} due to {} failed attempts. Locked until: {}",
                    user.getEmail(), attempts, lockUntil);
        }
    }

    public static void resetFailedAttempts(UserModel user) {
        if (user.getFailedLoginAttempts() != null && user.getFailedLoginAttempts() > 0) {
            LOGGER.info("Resetting failed login attempts for user: {}", user.getEmail());
        }
        user.setFailedLoginAttempts(0);
        user.setAccountLockedUntil(null);
    }

    public static void resetFailedAttempts(NonMemberUserModel user) {
        if (user.getFailedLoginAttempts() != null && user.getFailedLoginAttempts() > 0) {
            LOGGER.info("Resetting failed login attempts for non-member user: {}", user.getEmail());
        }
        user.setFailedLoginAttempts(0);
        user.setAccountLockedUntil(null);
    }

    public static long getRemainingLockTimeMinutes(UserModel user) {
        if (user.getAccountLockedUntil() == null) {
            return 0;
        }

        LocalDateTime now = LocalDateTime.now();
        if (now.isAfter(user.getAccountLockedUntil())) {
            return 0;
        }
        return java.time.Duration.between(now, user.getAccountLockedUntil()).toMinutes();
    }

    public static long getRemainingLockTimeMinutes(NonMemberUserModel user) {
        if (user.getAccountLockedUntil() == null) {
            return 0;
        }

        LocalDateTime now = LocalDateTime.now();
        if (now.isAfter(user.getAccountLockedUntil())) {
            return 0;
        }
        return java.time.Duration.between(now, user.getAccountLockedUntil()).toMinutes();
    }
}

