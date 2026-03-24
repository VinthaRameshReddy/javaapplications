package com.medgo.reimburse.domain.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Data
@Builder
public class ReimHistoryDTO {

    // Basic Details
    private String patientFullName;
    private String serviceType;
    private String checkNo;
    private String controlCode;
    private String entryCode;
    private String memberCode;
    private String forRmdApproval;

    // Amounts
    private String requestedAmount;
    private String approvedAmount;

    // Dates
    private LocalDateTime availmentDate;
    private LocalDateTime receivedDate;
    private LocalDateTime entryDate;

    // Claim Information
    private String claimNature;

    // Status
    private String status;
    private String displayStatus;
    private String displayDescription;
    private String displayRemarks;
    private Map<String, List<String>> parsedRemarks;

    public String getDisplayRemarks() {
        // If parsedRemarks is present, ignore displayRemarks
        return parsedRemarks != null ? null : displayRemarks;
    }
}
