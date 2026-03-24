package com.medgo.auth.domain.entity.medigo;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.redis.core.RedisHash;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Map;

@RedisHash("MEDGO_USER_OTP")
@Data
@AllArgsConstructor
@Builder
@NoArgsConstructor
public class UserOTPModel implements Serializable {
    @Id
    private String otpRefId;
    private String token;
    private Long userId;

    @Builder.Default
    private int attempts = 0;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime expiresAt;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime lastSentAt;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime lockedUntil;

    private Map<String, Object> data;

    private String identifier;

    private String memberCode;
    private LocalDate dob;
    private String email;
    private String mobile;
    private String otp;
    private LocalDateTime otpGenTime;
    private boolean otpValidated;
}
