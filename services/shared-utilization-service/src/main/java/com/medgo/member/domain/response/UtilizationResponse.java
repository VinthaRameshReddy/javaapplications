package com.medgo.member.domain.response;

import jakarta.persistence.Id;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;


@Data
public class UtilizationResponse {

    @Id
    private String controlCode;
    private LocalDate availFr;
    private LocalDate availTo;
    private String diagDesc;
    private String dxRem;
    private String hospitalName;
    private String doctorName;
    private BigDecimal approved;
    private BigDecimal disapproved;
    private BigDecimal advances;
    private BigDecimal erc;
    private String memcode;
    private String patient;
    private String company;
    private LocalDate periodFr;
    private LocalDate periodTo;
    private String printedBy;
    private String billcode;
    private BigDecimal medicareIncentives;
    private String reimReason;
    private String updatedBy;
    private LocalDateTime updatedDate;
    private LocalDate valid;
    private LocalDate effective;
    private String hospSoa;
    private String icd10Code;
    private String icd10Desc;
    private String remarks2;
    private String checknum;
    private String pf;
    private String rcvdBy;
    private LocalDate rcvdDate;
    private String depname;
    private  String depcode;


    public String getCompany() {
        return company;
    }

    public void setCompany(String company) {
        this.company = company;
    }

    public String getPatient() {
        return patient;
    }

    public void setPatient(String patient) {
        this.patient = patient;
    }
}
