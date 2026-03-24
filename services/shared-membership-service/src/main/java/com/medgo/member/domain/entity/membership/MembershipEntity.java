package com.medgo.member.domain.entity.membership;


import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "vw_mcap_membership")

public class MembershipEntity {
    @Id
    @Column(name = "PRIN_CODE")
    private String memberCode;

    @Column(name = "PRINCIPAL_CODE")
    private String principalCode;

    @Column(name = "MEM_APPNUM")
    private String memberAppNum;

    @Column(name = "MEM_FNAME")
    private String firstName;

    @Column(name = "MEM_LNAME")
    private String lastName;

    @Column(name = "MEM_MI")
    private String middleName;

    @Column(name = "MEM_SEX")
    private Integer sex;

    @Column(name = "AGE")
    private String age;

    @Column(name = "MEM_BDAY")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime birthDate;

    @Column(name = "CIVIL_STATUS")
    private String civilStatus;

    @Column(name = "MEM_STATUS_CODE")
    private String memStatusCode;

    @Column(name = "MEM_STATUS")
    private String memStatus;

    @Column(name = "MEM_TYPE")
    private String memType;

    @Column(name = "Plan_Desc")
    private String planDesc;

    @Column(name = "EFF_DATE")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime effectivityDate;

    @Column(name = "VAL_DATE")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime validityDate;

    @Column(name = "RESIGN_DATE")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime resignDate;

    @Column(name = "MOTHER_CODE")
    private String motherCode;

    @Column(name = "ACCOUNT_CODE")
    private String accountCode;

    @Column(name = "ACCOUNT_NAME")
    private String accountName;

    @Column(name = "ACCOUNT_TYPE")
    private String accountType;

    @Column(name = "ID_REM")
    private String idRem;

    @Column(name = "ID_REM2")
    private String idRem2;

    @Column(name = "ID_REM3")
    private String idRem3;

    @Column(name = "ID_REM4")
    private String idRem4;

    @Column(name = "ID_REM5")
    private String idRem5;

    @Column(name = "ID_REM6")
    private String idRem6;

    @Column(name = "ID_REM7")
    private String idRem7;

    @Column(name = "OTHER_REM")
    private String otherRemarks;

    @Column(name = "RSPROOMRATE_ID")
    private Integer rspRoomRateId;
}