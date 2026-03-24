package com.medgo.member.domain.response;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class MembershipResponse {
    private String memberCode;
    private String principalCode;
    private String memberAppNum;
    private String firstName;
    private String lastName;
    private String middleName;
    private Integer sex;
    private String age;
    private LocalDateTime birthDate;
    private String civilStatus;
    private String memStatusCode;
    private String memStatus;
    private String memType;
    private String planDesc;
    private LocalDateTime effectivityDate;
    private LocalDateTime validityDate;
    private LocalDateTime resignDate;
    private String motherCode;
    private String accountCode;
    private String accountName;
    private String accountType;
    private String idRem;
    private String idRem2;
    private String idRem3;
    private String idRem4;
    private String idRem5;
    private String idRem6;
    private String idRem7;
    private String otherRemarks;
    private Integer rspRoomRateId;
}