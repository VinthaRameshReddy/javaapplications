package com.medgo.loaservice.domain.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoaRequestDTO {

    @JsonProperty("serviceId")
    @NotNull(message = "serviceId is required")
    @Min(value = 1, message = "serviceId must be greater than 0")
    private Long serviceId;

    @JsonProperty("serviceSubTypeId")
    @Min(value = 1, message = "serviceSubTypeId must be greater than 0")
    private Long serviceSubTypeId;

    @JsonProperty("originMemberCode")
    private String originMemberCode;

    @Valid
    @NotNull(message = "request is required")
    private LoaTransactionRequestDTO request;

    @Valid
    private TelemedicineRequestDTO telemedicine;
}
