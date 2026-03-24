package com.medgo.member.domain.entity.utilization;



import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Entity
@AllArgsConstructor
@NoArgsConstructor
@NamedStoredProcedureQueries({
        @NamedStoredProcedureQuery(
                name = "CP_CLMS_REP_UTILIZATION_ALL_V3_API",  // Name of your stored procedure
                procedureName = "CP_CLMS_REP_UTILIZATION_ALL_V3_API",
                resultClasses = {UtilizationLegacyEntity.class}, // Map the result to this entity
                parameters = {
                        @StoredProcedureParameter(name = "@DATE_FR", mode = ParameterMode.IN, type = LocalDateTime.class),
                        @StoredProcedureParameter(name = "@DATE_TO", mode = ParameterMode.IN, type = LocalDateTime.class),
                        @StoredProcedureParameter(name = "@MEMCODE", mode = ParameterMode.IN, type = String.class),
                        @StoredProcedureParameter(name = "@LNAME", mode = ParameterMode.IN, type = String.class),
                        @StoredProcedureParameter(name = "@FNAME", mode = ParameterMode.IN, type = String.class),
                        @StoredProcedureParameter(name = "@MI", mode = ParameterMode.IN, type = String.class),
                        @StoredProcedureParameter(name = "@COMP", mode = ParameterMode.IN, type = String.class),
                        @StoredProcedureParameter(name = "@USER", mode = ParameterMode.IN, type = String.class),
                        @StoredProcedureParameter(name = "@VAL_DATE", mode = ParameterMode.IN, type = String.class),
                        @StoredProcedureParameter(name = "@EFF_DATE", mode = ParameterMode.IN, type = String.class)
                }
        ),
        @NamedStoredProcedureQuery(
                name = "CP_CLMS_REP_UTILIZATION_ALL_V5",
                procedureName = "CP_CLMS_REP_UTILIZATION_ALL_V5",
                resultClasses = { UtilizationLegacyEntity.class },
                parameters = {
                        @StoredProcedureParameter(name = "@DATE_FR",  mode = ParameterMode.IN, type = LocalDateTime.class),
                        @StoredProcedureParameter(name = "@DATE_TO",  mode = ParameterMode.IN, type = LocalDateTime.class),
                        @StoredProcedureParameter(name = "@MEMCODE",  mode = ParameterMode.IN, type = String.class),
                        @StoredProcedureParameter(name = "@LNAME",    mode = ParameterMode.IN, type = String.class),
                        @StoredProcedureParameter(name = "@FNAME",    mode = ParameterMode.IN, type = String.class),
                        @StoredProcedureParameter(name = "@MI",       mode = ParameterMode.IN, type = String.class),
                        @StoredProcedureParameter(name = "@COMP",     mode = ParameterMode.IN, type = String.class),
                        @StoredProcedureParameter(name = "@USER",     mode = ParameterMode.IN, type = String.class),
                        @StoredProcedureParameter(name = "@VAL_DATE", mode = ParameterMode.IN, type = String.class),
                        @StoredProcedureParameter(name = "@EFF_DATE", mode = ParameterMode.IN, type = String.class)
                }
        )
})

public class UtilizationLegacyEntity {

//    @Id  // Check what is ID?
//    @Column(name = "CONTROL_CODE")
//    private String controlCode; //  Map to a column from your result set

    @EmbeddedId
    private UtilizationLegacyId id;

    @Column(name = "AVAIL_FR")
    private LocalDateTime availFr;

    @Column(name = "AVAIL_TO")
    private LocalDateTime availTo;

    @Column(name = "DIAG_DESC")
    private String diagDesc;

    @Column(name = "DX_REM")
    private String dxRem;

    @Column(name = "HOSPITAL_NAME")
    private String hospitalName;

    private String doctorName;

    @Column(name = "APPROVED")
    private Double approved;

    @Column(name = "DISAPPROVED")
    private Double disapproved;

    @Column(name = "ADVANCES")
    private Double advances;

    @Column(name = "ERC")
    private Double erc;

    @Column(name = "MEMCODE")
    private String memcode;

    @Column(name = "PATIENT")
    private String patient;

    @Column(name = "COMPANY")
    private String company;

    @Column(name = "PERIOD_FR")
    private LocalDateTime periodFr;

    @Column(name = "PERIOD_TO")
    private LocalDateTime periodTo;

    @Column(name = "PRINTED_BY")
    private String printedBy;

    @Column(name = "BILLCODE")
    private String billcode;

    @Column(name = "MEDICARE_INCENTIVES")
    private Double medicareIncentives;

    @Column(name = "REIM_REASON")
    private String reimReason;

    @Column(name = "UPDATED_BY")
    private String updatedBy;

//    @Column(name = "UPDATED_DATE")
//    private LocalDateTime updatedDate;

    @Column(name = "valid")
    private String valid;

    @Column(name = "Effective")
    private String effective;

    @Column(name = "HOSP_SOA")
    private String hospSoa;

    @Column(name = "ICD10_CODE")
    private String icd10code;

    @Column(name = "ICD10_DESC")
    private String icd10desc;

    @Column(name = "REMARKS2")
    private String remarks2;

    @Column(name = "CHECKNUM")
    private String checknum;

    @Column(name = "PF")
    private String pf;

    @Column(name = "RCVD_BY")
    private String rcvdBy;

    @Column(name = "RCVD_DATE")
    private LocalDateTime rcvdDate;

    private String depname;

    private String depcode;

    public void setId(UtilizationLegacyId utilizationLegacyId) {
    }
}