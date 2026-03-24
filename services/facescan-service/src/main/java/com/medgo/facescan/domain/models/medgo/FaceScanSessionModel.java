package com.medgo.facescan.domain.models.medgo;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.time.LocalDateTime;

@Entity
@Table(name = "FACE_SCAN_SESSION")
@Data
@NoArgsConstructor
@ToString
public class FaceScanSessionModel extends BaseEntity {

    @Column(name="FED_ID", nullable = false, unique = true)
    private String fedId;

    @Column(name = "MEMBER_CODE")
    private String memberCode;

    @Column(name="SESSION_ID")
    private String sessionId;

    @Column(name = "CONSENT")
    private boolean consent;

    @Column(name = "CREATED_TIME")
    private LocalDateTime creationTime;

    @Column(name = "AGE")
    private Integer age;

    @Column(name = "MEMBER_NAME")
    private String memberName;

    @Column(name = "GENDER")
    private String gender;

    @Column(name = "WEIGHT")
    private Double weight;

    @Column(name = "HEIGHT")
    private Double height;

    @Column(name = "IS_SMOKER")
    private String isSmoker;

    @Column(name = "IS_DIABETIC")
    private String isDiabetic;

    @Column(name = "BP_MEDICATION")
    private String bpMedication;

    @Column(name = "SESSION_END_TIME")
    private LocalDateTime sessionEndTime;

    @Column(name = "STATUS")
    private String status;

    public FaceScanSessionModel(String fedId, String memberCode) {
        this.fedId = fedId;
        this.memberCode = memberCode;
    }
}
