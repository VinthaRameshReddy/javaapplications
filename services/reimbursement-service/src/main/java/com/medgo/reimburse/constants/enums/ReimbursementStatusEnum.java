package com.medgo.reimburse.constants.enums;

import lombok.Getter;

@Getter
public enum ReimbursementStatusEnum {
    CREDITED("Credited"),
    CHECK_RELEASED("Check Released"),
    TRANSACTED("Transacted"),
    CHECK_FOR_RELEASE("Check For Release"),
    WITH_MEMBERSHIP_CONCERN("With Membership Concern"),
    ON_HOLD_WITH_PENDING_REQUIREMENTS("On Hold With Pending Requirements"),
    WITH_PENDING_BILLING_CONCERN("With Pending Billing Concern"),
    FOR_VALIDATION_OF_BILLING_DEPARTMENT("For Validation of Billing Department"),
    FOR_CHECK_PREPARATION("For Check Preparation"),
    FOR_PAYMENT_PROCESSING_THRU_E_PAYOUT("For Payment Processing thru E-payout"),
    DISAPPROVED("Disapproved"),
    WITH_ACTION_MEMO("With Action Memo"),
    WITH_LACKING_REQUIREMENTS("With Lacking Requirements"),
    IN_PROCESS("In Process"),
    RECEIVED("Received"),

    //STATUS BELOW ARE V1 COPIES AND TAGGED AS DEPRECATED START -->
//    CHECK_FOR_RELEASE("Check For Release"),
//    CHECK_RELEASED("Check Released"),
//    CREDITED("Credited"),
//    DISAPPROVED("Disapproved"),
    FOR_APPROVAL("For Approval"),
//    FOR_CHECK_PREPARATION("For check preparation"),
    FOR_PROCESSING("For Processing"),
    FOR_RELEASE("For release"),
    FOR_REVISION("For Revision"),
    HOSPITAL_BILLS("Hospital Bills"),
//    IN_PROCESS("In Process"),
    LACKING_OF_DOCUMENTS("Lacking Of Documents"),
    ON_HOLD_BILLING("On Hold By Billing"),
//    ON_HOLD_WITH_PENDING_REQUIREMENTS("On Hold With Pending Requirements"),
    PAYMENT_IN_PROCESS("Payment In Process"),
    PAYMENT_PREPARATION("For Payment Preparation"),
    PROCESSED_WITH_CHECK("Processed With Check"),
    READY_FOR_RELEASE("Ready for release"),
//    RECEIVED("Received"),
    RELEASED("Released"),
    WAITING_HOSPITAL_BILL("Waiting for hospital bills"),
//    WITH_ACTION_MEMO("With Action Memo"),
    WITH_BILL_CONCERN("With billing concern"),
    WITH_LACKING_DOCUMENTS("With lacking documents"),
//    WITH_LACKING_REQUIREMENTS("With Lacking Requirements"),
//    WITH_MEMBERSHIP_CONCERN("With Membership Concern"),
    WITH_PREMIUM_CONCERN("With Premium Concern"),
    WITH_URG_CONCERN("With URG Concern"),;

    private String value;

    ReimbursementStatusEnum(String value) {
        this.value = value;
    }
}
