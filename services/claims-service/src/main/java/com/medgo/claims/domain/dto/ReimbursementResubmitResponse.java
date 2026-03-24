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
public class ReimbursementResubmitResponse {
    private Long id; // Unique ID from database
    private String controlCode; // Original control code (retained)
    private String status;
    private LocalDateTime requestDate;
    private LocalDate dateOfTreatment;
    private BigDecimal totalClaimAmount;
    private String patientName;
    private String memberCode;
    private String serviceType;
    private String natureOfClaim;
    private String disbursementMethod;
    private String bankName;
    private String bankAccountNumber;
    private String fullAccountName;
    private List<DocumentResponse> documents;
    private String message; // Confirmation message
}

