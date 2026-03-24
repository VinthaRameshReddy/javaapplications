package com.medgo.loaservice.domain.dto.request;

import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RequestSteerageDTO {

    private Long id;

    private Boolean steerage;

    private String hospitalCode;

    private Long serviceSteerageId;

    @Min(value = 1, message = "Cost saving should not be below 1.")
    private BigDecimal costSaving;
}
