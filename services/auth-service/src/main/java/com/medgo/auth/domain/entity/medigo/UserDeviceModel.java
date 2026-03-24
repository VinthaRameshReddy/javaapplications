package com.medgo.auth.domain.entity.medigo;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "med_user_device", schema = "dbo")
@EqualsAndHashCode(callSuper = true)
public class UserDeviceModel extends BaseEntity {

    @Column(name = "USER_ID")
    private String userId;   // now VARCHAR

    @Column(name = "EMAIL", nullable = false)
    private String email;

    @Column(name = "PLATFORM")
    private String platform;

    @Column(name = "DEVICE_ID", nullable = false)
    private String deviceId;

    @Column(name = "DEVICE_BRAND")
    private String deviceBrand;

    @Column(name = "DEVICE_MODEL")
    private String deviceModel;

    @Column(name = "DEVICE_OS")
    private String deviceOs;

    @Column(name = "LOCATION")
    private String location;

    @Column(name = "ENABLED", length = 1)
    private String enabled = "N";   // store as 'Y' or 'N'

    @Column(name = "LAST_LOGIN_AT")
    private LocalDateTime lastLoginAt;
}
