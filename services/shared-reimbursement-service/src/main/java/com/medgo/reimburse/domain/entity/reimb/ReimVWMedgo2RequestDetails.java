package com.medgo.reimburse.domain.entity.reimb;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Immutable;
import org.hibernate.annotations.Subselect;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Immutable
@Subselect("""
    SELECT 
        ROW_NUMBER() OVER (
            ORDER BY MEMBER_CODE, CONTROL_CODE, ENTRY_CODE, AVAILMENT_DATE
        ) AS id,
        SERVICE_TYPE,
        CHECK_NO,
        MEMBER_CODE,
        CONTROL_CODE,
        ENTRY_CODE,
        MEM_LNAME,
        MEM_FNAME,
        MEM_MNAME,
        HOLD_REMARKS,
        LAPSE_PAYMENT_REMARKS,
        FOR_RMD_APPROVAL,
        REQUESTED_AMOUNT,
        APPROVED_AMOUNT,
        AVAILMENT_DATE,
        RECEIVED_DATE,
        ENTRY_DATE,
        CLAIM_NATURE,
        ACTION_MEMO_REMARKS,
        DENIED_MEMO_REMARKS,
        WHB_MEMO_REMARKS,
        ACTION_MEMO,
        DENIED_MEMO,
        WHB_MEMO,
        STATUS
    FROM Reim_VW_MEDGO2_Request_Details
""")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReimVWMedgo2RequestDetails {

    @Id
    private Long id;

    @Column(name = "SERVICE_TYPE")
    private String serviceType;

    @Column(name = "CHECK_NO")
    private String checkNo;

    @Column(name = "MEMBER_CODE")
    private String memberCode;

    @Column(name = "CONTROL_CODE")
    private String controlCode;

    @Column(name = "ENTRY_CODE")
    private String entryCode;

    @Column(name = "MEM_LNAME")
    private String memLname;

    @Column(name = "MEM_FNAME")
    private String memFname;

    @Column(name = "MEM_MNAME")
    private String memMname;

    @Column(name = "HOLD_REMARKS")
    private String holdRemarks;

    @Column(name = "LAPSE_PAYMENT_REMARKS")
    private String lapsePaymentRemarks;

    @Column(name = "FOR_RMD_APPROVAL")
    private String forRmdApproval;

    @Column(name = "REQUESTED_AMOUNT", precision = 38, scale = 2)
    private BigDecimal requestedAmount;

    @Column(name = "APPROVED_AMOUNT", precision = 38, scale = 2)
    private BigDecimal approvedAmount;

    @Column(name = "AVAILMENT_DATE")
    private LocalDateTime availmentDate;

    @Column(name = "RECEIVED_DATE")
    private LocalDateTime receivedDate;

    @Column(name = "ENTRY_DATE")
    private LocalDateTime entryDate;

    @Column(name = "CLAIM_NATURE")
    private String claimNature;

    @Column(name = "ACTION_MEMO_REMARKS")
    private String actionMemoRemarks;

    @Column(name = "DENIED_MEMO_REMARKS")
    private String deniedMemoRemarks;

    @Column(name = "WHB_MEMO_REMARKS")
    private String whbMemoRemarks;

    @Column(name = "ACTION_MEMO")
    private Boolean actionMemo;

    @Column(name = "DENIED_MEMO")
    private Boolean deniedMemo;

    @Column(name = "WHB_MEMO")
    private Boolean whbMemo;

    @Column(name = "STATUS")
    private String status;
}
