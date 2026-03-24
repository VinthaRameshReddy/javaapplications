package com.medgo.auth.domain.entity.medigo;


import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.hibernate.annotations.ColumnDefault;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Entity
@Table(name = "medgo_users", schema = "dbo")
@EqualsAndHashCode(callSuper = true)
public class UserModel extends BaseEntity {
    @Column(name = "USER_ROLE_ID")
    private Long userRoleId;

    @Column(name = "USERNAME", length = 50)
    private String username;

    @Column(name = "PASSWORD")
    private String password;

    @Column(name = "MOBILE", nullable = true, length = 11)
    private String mobile;

    @Column(name = "EMAIL", nullable = false, length = 100, unique = true)
    private String email;

    @ColumnDefault("'INACTIVE'")
    @Enumerated(EnumType.STRING)
    @Column(name = "STATUS", nullable = false, length = 50)
    private MedGoUserStatusEnum status;

    @Column(name = "MEMBER_CODE", length = 50)
    private String memberCode;

    @Column(name = "ACCOUNT_CODE", length = 50)
    private String accountCode;

    @Column(name = "LAST_NAME", length = 100)
    private String lastName;

    @Column(name = "FIRST_NAME", length = 100)
    private String firstName;

    @Column(name = "MIDDLE_NAME", length = 100)
    private String middleName;

    @Column(name = "BIRTH_DATE")
    private LocalDate birthDate;

    @Column(name = "SEX")
    private Integer sex;

    @Column(name = "ORIGIN")
    @Enumerated(EnumType.STRING)
    private MedGoUserOriginEnum origin;

    @Column(name = "OTP_EXCLUDED")
    private Boolean excludedFromOtp;

    @Column(name = "CHANGE_PASS_EXCLUDED")
    private Boolean excludedFromChangePassword;

    @Column(name = "PASSWORD_EXPIRES_AT")
    private LocalDateTime passwordExpiresAt;

    @Column(name = "OTP_REQUESTED_AT")
    private LocalDateTime otpRequestedAt;

    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @JoinColumn(name = "ID")
    private UserRoleModel role;

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER, orphanRemoval = true)
    @JoinColumn(name = "USER_ID", referencedColumnName = "ID")
    private List<UserPassHistoryModel> pastPasswords;

    @Column(name = "OTP", length = 6)
    private String otp;

    @Column(name = "FAILED_OTP_ATTEMPTS")
    private Integer failedOtpAttempts = 0;

    // Newly added column: last OTP sent timestamp
    @Column(name = "LAST_OTP_SENT_AT")
    private LocalDateTime lastOtpSentAt;

    // Newly added column: registration status (e.g., "INACTIVE", "ACTIVE")
    @Column(name = "REGISTRATION_STATUS")
    private String registrationStatus;

    // Login attempt tracking columns
    @Column(name = "FAILED_LOGIN_ATTEMPTS")
    private Integer failedLoginAttempts = 0;

    @Column(name = "ACCOUNT_LOCKED_UNTIL")
    private LocalDateTime accountLockedUntil;

    @Column(name = "ACCOUNT_UNLOCK_TIME")
    private LocalDateTime accountUnlockTime;

    @Column(name = "FAILED_DELETE_ACCOUNT_ATTEMPTS")
    private Integer failedDeleteAccountAttempts = 0;

    @Column(name = "DELETE_ACCOUNT_LOCK_TIME")
    private LocalDateTime deleteAccountLockTime;

    @Column(name = "BIOMETRIC_ENABLED", nullable = true)
    private Boolean biometricEnabled ;

    @Column(name = "PASSKEY", columnDefinition = "TEXT")
    private String passkey; // Stores passkeyHash (SHA256 hash)

    @Column(name = "PASSKEY_CREATED_AT")
    private LocalDateTime passkeyCreatedAt;

    @Column(name = "IS_WHITELISTED", length = 1)
    @ColumnDefault("'N'")
    private String isWhitelisted; // 'Y' or 'N'

    @Column(name = "IS_CONSENTED")
    @ColumnDefault("0")
    private Boolean isConsented = false;

}


