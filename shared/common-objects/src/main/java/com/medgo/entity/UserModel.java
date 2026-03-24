package com.medgo.entity;


import com.medgo.enums.MedGoUserOriginEnum;
import com.medgo.enums.MedGoUserStatusEnum;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.hibernate.annotations.ColumnDefault;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Entity(name = "MEDGO_USERS")
@EqualsAndHashCode(callSuper = true)
public class UserModel extends BaseEntity {
    @Column(name = "USER_ROLE_ID")
    private Long userRoleId;

    @Column(name = "USERNAME", nullable = false, length = 50)
    private String username;

    @Column(name = "PASSWORD")
    private String password;


    @Column(name = "MOBILE", nullable = true, length = 11)
    private String mobile;

    @Column(name = "EMAIL", nullable = true, length = 100)
    private String email;

    @ColumnDefault("'INACTIVE'")
    @Enumerated(EnumType.STRING)
    @Column(name = "STATUS", nullable = false, length = 50)
    private MedGoUserStatusEnum status;

    @Column(name = "NON_MEMBER_CODE", length = 50)
    private String nonMemberCode;

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
    private boolean excludedFromOtp;

    @Column(name = "CHANGE_PASS_EXCLUDED")
    private boolean excludedFromChangePassword;

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


    // ✅ Newly added column: last OTP sent timestamp
    @Column(name = "LAST_OTP_SENT_AT")
    private LocalDateTime lastOtpSentAt;

    // ✅ Newly added column: registration status (e.g., "INACTIVE", "ACTIVE")
    @Column(name = "REGISTRATION_STATUS")
    private String registrationStatus;

    @Column(name = "ACCOUNT_UNLOCK_TIME")
    private LocalDateTime accountUnlockTime;



}


