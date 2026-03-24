package com.medgo.auth.domain.entity.medigo;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.ColumnDefault;

@Entity
@Table(name = "MEDGO_TRUSTED_DEVICES")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TrustDeviceEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long userId;

    private  String userType;

    @Column(nullable = false)
    private String deviceId;

    @Column(nullable = false)
    private String status; // "Y" or "N"

    @Column(name = "BIOMETRIC_ENABLED", nullable = true)
    private Boolean biometricEnabled ;

}
