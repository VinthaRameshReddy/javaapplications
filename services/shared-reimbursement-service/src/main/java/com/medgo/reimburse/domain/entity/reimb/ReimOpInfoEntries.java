package com.medgo.reimburse.domain.entity.reimb;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "reim_op_info_entries", schema = "dbo", catalog = "ReimDB")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReimOpInfoEntries {

    @Id
    @Column(name = "entry_code", length = 25, nullable = false)
    private String entryCode;

    @Column(name = "control_code", length = 25, nullable = false)
    private String controlCode;

    @Column(name = "first_entry", nullable = false)
    @Builder.Default
    private Boolean firstEntry = false;

    @Column(name = "member_code", length = 50, nullable = false)
    private String memberCode;

    @Column(name = "mem_lname", length = 50, nullable = false)
    private String memLname;

    @Column(name = "mem_fname", length = 50, nullable = false)
    private String memFname;

    @Column(name = "mem_mname", length = 50)
    private String memMname;

    @Column(name = "mem_age", nullable = false)
    private Float memAge;

    @Column(name = "mem_type", nullable = false)
    private Short memType;

    @Column(name = "mem_status", nullable = false)
    private Integer memStatus;

    @Column(name = "mem_effectivity")
    private LocalDateTime memEffectivity;

    @Column(name = "mem_validity")
    private LocalDateTime memValidity;

    @Column(name = "prin_code", length = 25)
    private String prinCode;

    @Column(name = "company_code", length = 50, nullable = false)
    private String companyCode;

    @Column(name = "plan_desc", length = 50)
    private String planDesc;

    @Column(name = "visit_date", nullable = false)
    private LocalDateTime visitDate;

    @Column(name = "ars_no", length = 25)
    private String arsNo;

    @Column(name = "first_life", nullable = false)
    @Builder.Default
    private Boolean firstLife = false;

    @Column(name = "hospital_code", length = 25)
    private String hospitalCode;

    @Column(name = "is_doctor", nullable = false)
    @Builder.Default
    private Boolean isDoctor = false;

    @Column(name = "doc_dentist_code", length = 50)
    private String docDentistCode;

    @Column(name = "diag_code", length = 25, nullable = false)
    private String diagCode;

    @Column(name = "limit", length = 20)
    private String limit;

    @Column(name = "type", length = 20)
    private String type;

    @Column(name = "proc_type", length = 20)
    private String procType;

    @Column(name = "dx_remarks", columnDefinition = "varchar(max)")
    private String dxRemarks;

    @Column(name = "special_remarks", columnDefinition = "varchar(max)")
    private String specialRemarks;

    @Column(name = "classification_code")
    private Integer classificationCode;

    @Column(name = "contract_coverage", columnDefinition = "varchar(max)")
    private String contractCoverage;

    @Column(name = "disapproved_remarks", columnDefinition = "varchar(max)")
    private String disapprovedRemarks;

    @Column(name = "inquiry_rpt_id", length = 12)
    private String inquiryRptId;

    @Column(name = "extracted_by", length = 25)
    private String extractedBy;

    @Column(name = "extracted_date")
    private LocalDateTime extractedDate;

    @Column(name = "entry_by", length = 25, nullable = false)
    private String entryBy;

    @Column(name = "entry_date", nullable = false)
    private LocalDateTime entryDate;

    @Column(name = "updated_by", length = 25, nullable = false)
    private String updatedBy;

    @Column(name = "updated_date", nullable = false)
    private LocalDateTime updatedDate;
}
