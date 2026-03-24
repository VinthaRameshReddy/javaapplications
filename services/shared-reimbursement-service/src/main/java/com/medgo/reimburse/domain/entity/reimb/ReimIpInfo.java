package com.medgo.reimburse.domain.entity.reimb;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "reim_ip_info", schema = "dbo", catalog = "ReimDB")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReimIpInfo {

    @Id
    @Column(name = "control_code", length = 25, nullable = false)
    private String controlCode;

    @Column(name = "payee_type", nullable = false)
    private Short payeeType;

    @Column(name = "other_payee", length = 100, nullable = false)
    private String otherPayee;

    @Column(name = "target_date")
    private LocalDateTime targetDate;

    @Column(name = "ars_no", length = 25)
    private String arsNo;

    @Column(name = "admission_type")
    private Byte admissionType;

    @Column(name = "plan_desc", length = 50)
    private String planDesc;

    @Column(name = "pec_limit", length = 350)
    private String pecLimit;

    @Column(name = "rem_limit", length = 25)
    private String remLimit;

    @Column(name = "plan_availed", length = 125)
    private String planAvailed;

    @Column(name = "room_rate", columnDefinition = "money")
    private BigDecimal roomRate;

    @Column(name = "no_days", precision = 4, scale = 2)
    private BigDecimal noDays;

    @Column(name = "plan_availed1", length = 125)
    private String planAvailed1;

    @Column(name = "room_rate1", columnDefinition = "money")
    private BigDecimal roomRate1;

    @Column(name = "no_days1", precision = 4, scale = 2)
    private BigDecimal noDays1;

    @Column(name = "plan_availed2", length = 125)
    private String planAvailed2;

    @Column(name = "room_rate2", columnDefinition = "money")
    private BigDecimal roomRate2;

    @Column(name = "no_days2", precision = 4, scale = 2)
    private BigDecimal noDays2;

    @Column(name = "plan_availed3", length = 125)
    private String planAvailed3;

    @Column(name = "room_rate3", columnDefinition = "money")
    private BigDecimal roomRate3;

    @Column(name = "no_days3", precision = 4, scale = 2)
    private BigDecimal noDays3;

    @Column(name = "diagnosis", length = 25)
    private String diagnosis;

    @Column(name = "type", length = 10)
    private String type;

    @Column(name = "limit", length = 50)
    private String limit;

    @Column(name = "proc_type", length = 50)
    private String procType;

    @Column(name = "other_diagnosis", length = 300)
    private String otherDiagnosis;

    @Column(name = "special_remarks", length = 300)
    private String specialRemarks;

    @Column(name = "classification_code")
    private Integer classificationCode;

    @Column(name = "contract_coverage", length = 1000)
    private String contractCoverage;

    @Column(name = "disapproved_remarks", length = 1000)
    private String disapprovedRemarks;

    @Column(name = "complied")
    private Boolean complied;

    @Column(name = "complied_date")
    private LocalDateTime compliedDate;

    @Column(name = "complied_due_date")
    private LocalDateTime compliedDueDate;

    @Column(name = "complied_target_date")
    private LocalDateTime compliedTargetDate;

    @Column(name = "complied_remarks", length = 1000)
    private String compliedRemarks;

    @Column(name = "rmd_status")
    private Short rmdStatus;

    @Column(name = "rmd_hold_rem", length = 1000)
    private String rmdHoldRem;

    @Column(name = "rmd_lapse_rem", length = 1000)
    private String rmdLapseRem;

    @Column(name = "rmd_complied")
    private Boolean rmdComplied;

    @Column(name = "rmd_complied_date")
    private LocalDate rmdCompliedDate;

    @Column(name = "memo_type", nullable = false)
    private Byte memoType;

    @Column(name = "action_memo_id", length = 12)
    private String actionMemoId;

    @Column(name = "disapproved_memo_id", length = 12)
    private String disapprovedMemoId;

    @Column(name = "empty_memo_id", length = 12)
    private String emptyMemoId;

    @Column(name = "whb_memo_id", length = 12)
    private String whbMemoId;

    @Column(name = "inquiry_rpt_id", length = 12)
    private String inquiryRptId;

    @Column(name = "incident_rpt_id", length = 12)
    private String incidentRptId;

    @Column(name = "processing_status_id", length = 12)
    private String processingStatusId;

    @Column(name = "extracted_by", length = 25)
    private String extractedBy;

    @Column(name = "extracted_date")
    private LocalDateTime extractedDate;

    @Column(name = "entry_by", length = 25, nullable = false)
    private String entryBy;

    @Column(name = "entry_date", nullable = false)
    private LocalDateTime entryDate;

    @Column(name = "updated_by", length = 25)
    private String updatedBy;

    @Column(name = "updated_date")
    private LocalDateTime updatedDate;
}
