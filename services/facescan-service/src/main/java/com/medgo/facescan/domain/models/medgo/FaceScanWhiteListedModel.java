package com.medgo.facescan.domain.models.medgo;


import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "FACE_SCAN_WHITELISTED")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FaceScanWhiteListedModel extends BaseEntity {

    @Column(name = "ACCOUNT_CODE", nullable = false, unique = true)
    private String accountCode;

    @Column(name = "MEMBER_TYPE")
    private Character memberType;

    @Column(name = "START_DATE")
    private LocalDateTime startDate;

    @Column(name = "END_DATE")
    private LocalDateTime endDate;
}