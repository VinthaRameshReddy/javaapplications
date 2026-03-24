package com.medgo.reimburse.domain.dto;

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
public class ReimbursementSubmissionRequest {
    private String memberCode;
    private String patientName;
    private String serviceType;
    private String natureOfClaim;
    private String controlCode;
    private String hospitalClinicName;
    private String hospitalClinicAddress;
    private String hospitalClinicPhone;
    private String requestingDoctor;
    private LocalDate confinementDate;
    private LocalDate dischargeDate;
    private LocalDate availmentDate;
    private LocalDate dateOfTreatment;
    private BigDecimal totalClaimAmount;
    private String disbursementMethod;
    private String bankName;
    private String bankBranch;
    private String fullAccountName;
    private String bankAccountNumber;
    private List<DocumentInfo> documents;
}
