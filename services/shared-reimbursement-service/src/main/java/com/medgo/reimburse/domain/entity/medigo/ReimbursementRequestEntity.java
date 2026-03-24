package com.medgo.reimburse.domain.entity.medigo;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "reimbursement_requests")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReimbursementRequestEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "control_code", unique = true, nullable = false, length = 50)
    private String controlCode;

    @Column(name = "entry_code", unique = true, length = 50)
    private String entryCode;

    @Column(name = "principal_code", length = 50)
    private String principalCode;

    @Column(name = "company_code", length = 50)
    private String companyCode;

    @Column(name = "mem_type")
    private Short memType;

    @Column(name = "mem_age")
    private Float memAge;

    @Column(name = "costplus_code")
    private Integer costplusCode;

    @Column(name = "particulars_code")
    private Integer particularsCode;

    @Column(name = "member_code", nullable = false, length = 50)
    private String memberCode;

    @Column(name = "patient_name", nullable = false, length = 255)
    private String patientName;

    @Column(name = "service_type", nullable = false, length = 50)
    private String serviceType;

    @Column(name = "nature_of_claim", nullable = false, length = 100)
    private String natureOfClaim;

    @Column(name = "hospital_clinic_name", nullable = false, length = 255)
    private String hospitalClinicName;

    @Column(name = "hospital_clinic_address", length = 500)
    private String hospitalClinicAddress;

    @Column(name = "hospital_clinic_phone", length = 50)
    private String hospitalClinicPhone;

    @Column(name = "requesting_doctor", nullable = false, length = 255)
    private String requestingDoctor;

    @Column(name = "confinement_date")
    private LocalDate confinementDate;

    @Column(name = "discharge_date")
    private LocalDate dischargeDate;

    @Column(name = "availment_date")
    private LocalDate availmentDate;

    @Column(name = "date_of_treatment")
    private LocalDate dateOfTreatment;

    @Column(name = "total_claim_amount", nullable = false, precision = 18, scale = 2)
    private BigDecimal totalClaimAmount;

    @Column(name = "disbursement_method", length = 50)
    private String disbursementMethod;

    @Column(name = "bank_name", length = 255)
    private String bankName;

    @Column(name = "bank_branch", length = 255)
    private String bankBranch;

    @Column(name = "full_account_name", length = 255)
    private String fullAccountName;

    @Column(name = "bank_account_number", length = 50)
    private String bankAccountNumber;

    @Column(name = "status", nullable = false, length = 50)
    @Builder.Default
    private String status = "In-Process";

    @Column(name = "request_date", nullable = false)
    @Builder.Default
    private LocalDateTime requestDate = LocalDateTime.now();

    @Column(name = "created_at", nullable = false)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "return_reasons", length = 2000)
    private String returnReasons;

    /**
     * When non-null, indicates the submission/resubmission confirmation email was already sent for this request.
     * Used to prevent duplicate emails when client retries after timeout (e.g. large file uploads).
     */
    @Column(name = "submission_email_sent_at")
    private LocalDateTime submissionEmailSentAt;

    @OneToMany(mappedBy = "reimbursementRequest", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<ReimbursementDocumentEntity> documents;

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
