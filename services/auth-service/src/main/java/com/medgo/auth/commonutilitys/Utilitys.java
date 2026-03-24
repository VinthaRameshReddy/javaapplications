package com.medgo.auth.commonutilitys;

import com.medgo.auth.domain.entity.medigo.UserOTPModel;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.concurrent.TimeUnit;


public class Utilitys {

    public static boolean isLocked(RedisTemplate<String, UserOTPModel> redisTemplate,
                                   String identifier) {
        Boolean locked = redisTemplate.hasKey("otp:lock:" + identifier);
        return locked != null && locked;
    }

    public static void lockAccount(RedisTemplate<String, UserOTPModel> redisTemplate,
                                   String identifier,
                                   int lockMinutes) {
        redisTemplate.opsForValue()
                .set("otp:lock:" + identifier, new UserOTPModel(), lockMinutes, TimeUnit.MINUTES);
    }

    public static void removeLock(RedisTemplate<String, UserOTPModel> redisTemplate,
            String identifier) {
        redisTemplate.delete("otp:lock:" + identifier);
    }
}

