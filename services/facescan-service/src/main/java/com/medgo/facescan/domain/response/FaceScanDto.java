package com.medgo.facescan.domain.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FaceScanDto {
    private String message;
    private String federatedMembershipCode;
    private Map<String,Object> lastScanData;
    private Map<String,Object> sessionFields;
}
