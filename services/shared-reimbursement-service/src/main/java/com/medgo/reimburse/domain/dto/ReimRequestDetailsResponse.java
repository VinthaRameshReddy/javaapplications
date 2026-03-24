package com.medgo.reimburse.domain.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ReimRequestDetailsResponse {
    
    private String controlCode;
    private String status;
    private String totalRequestedAmount;
    private String approvedAmount;
    private String requestDate;
    private String availmentDate;
    private String totalDisapprovedAmount;
    private List<DisapprovedItem> disapprovedItems;
}

