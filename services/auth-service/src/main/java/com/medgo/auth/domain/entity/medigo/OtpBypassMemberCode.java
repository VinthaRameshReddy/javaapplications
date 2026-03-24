package com.medgo.auth.domain.entity.medigo;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "OTP_BYPASS_MEMBER_CODES")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OtpBypassMemberCode {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "MEMBER_CODE", unique = true, nullable = false, length = 50)
    private String memberCode;

    @Column(name = "DEFAULT_OTP", nullable = false, length = 6)
    @Builder.Default
    private String defaultOtp = "123456";

    @Column(name = "IS_ACTIVE", nullable = false)
    @Builder.Default
    private Boolean isActive = true;
}

