package com.medgo.loaservice.domain.dto.request;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProcedureDTO {
    private boolean temporary;
    private List<String> diagnosisCodes;

    private String procedureCode;
    private String procedureName;
    private BigDecimal procedureAmount;

    @JsonIgnore
    private Long procedureCostCenterId;
    @JsonIgnore
    private Long serviceDiagnosisTypeId;
}
