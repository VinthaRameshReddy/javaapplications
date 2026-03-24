package com.medgo.claims.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReimbursementReturnReasonsResponse {
    private String controlCode;
    private String status;
    private List<String> returnReasons; // List of required actions/reasons
}

