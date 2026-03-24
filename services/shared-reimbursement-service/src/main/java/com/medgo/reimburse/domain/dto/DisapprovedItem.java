package com.medgo.reimburse.domain.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class DisapprovedItem {
    private String itemName;
    private String doctorName;
    private String dateOfConsultation;
    private String amountClaimed;
    private String amountDisapproved;
    private String reasonForDisapproval;
}

