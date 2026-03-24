package com.medgo.facescan.domain.models.medgo;


import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "FACE_SCAN_MEMBER_WHITELISTED")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FaceScanMemberWhiteListedModel extends BaseEntity {

    @Column(name = "MEMBER_CODE", nullable = false, unique = true)
    private String memberCode;

    @Column(name = "MEMBER_TYPE")
    private Character memberType;

    @Column(name = "START_DATE")
    private LocalDateTime  startDate;

    @Column(name = "END_DATE")
    private LocalDateTime endDate;
}