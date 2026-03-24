package com.medgo.claims.domain.dto;

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
    private BigDecimal requestedAmount;
    private BigDecimal approvedAmount;

    // Dates
    private LocalDateTime availmentDate;
    private LocalDateTime receivedDate;
    private LocalDateTime entryDate;

    // Claim Information
    private String claimNature;

    // Memo Remarks
//    private String actionMemoRemarks;
//    private String deniedMemoRemarks;
//    private String whbMemoRemarks;
//    private String holdRemarks;
//    private String lapsePaymentRemarks;

    // Memo Flags
//    private boolean actionMemo;
//    private boolean deniedMemo;
//    private boolean whbMemo;

    // Status
    private String status;
    private String displayStatus;
    private String displayDescription;
    private String displayRemarks;
    private Map<String, List<String>> parsedRemarks;

    // Split Denied Memo Remarks (parsed sections)
    private String deniedMemoMain;
    private String deniedMemoReason;
    private String deniedMemoLimitation;

    public String getDisplayRemarks() {
        // If parsedRemarks is present, ignore displayRemarks
        return parsedRemarks != null ? null : displayRemarks;
    }
}
