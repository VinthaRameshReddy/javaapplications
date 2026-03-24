package com.medgo.claims.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReimbursementRequestDetailsResponse {
    
    // Request identification
    private Long id;
    private String controlCode;
    private String status;
    
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
    
    // Return reasons (if returned for correction)
    private List<String> returnReasons;
    
    // Existing documents with IDs (for removal)
    private List<DocumentResponse> documents;
    
    // Timestamps
    private LocalDateTime requestDate;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

