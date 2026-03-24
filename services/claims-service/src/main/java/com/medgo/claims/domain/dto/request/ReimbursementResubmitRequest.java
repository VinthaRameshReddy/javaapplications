package com.medgo.claims.domain.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReimbursementResubmitRequest {

    // Control code of the original request (required for resubmission)
    private String controlCode;

    // Member Information
    private String memberCode;
    private String patientName;
    private String serviceType; // Inpatient or Outpatient
    private String natureOfClaim; // Surgical, Consultation, etc.

    // Hospital/Clinic Information
    private String hospitalClinicName;
    private String hospitalClinicAddress;
    private String hospitalClinicPhone;

    // Doctor Information
    private String requestingDoctor;

    // Dates
    private LocalDate confinementDate;
    private LocalDate dischargeDate;
    private LocalDate availmentDate;
    private LocalDate dateOfTreatment;

    // Amount
    private BigDecimal totalClaimAmount;

    // Disbursement Information
    private String disbursementMethod;
    private String bankName;
    private String bankBranch;
    private String fullAccountName;
    private String bankAccountNumber;

    // Document management: IDs of documents to remove/replace
    private List<Long> documentIdsToRemove;

    // New documents to upload
    private List<DocumentUploadRequest> newDocuments;
}

