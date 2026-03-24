package com.medgo.loaservice.domain.dto.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MedGoConsultationDTO {

    @NotNull(message = "Service sub type id is required.")
    private Long serviceSubTypeId;

    @NotBlank(message = "Requesting hospital is required.")
    @Pattern(regexp = "^[A-Za-z0-9-]*$", message = "Hospital Code accepts alphanumeric and - only.")
    private String hospitalCode;

    @NotBlank(message = "Requesting doctor is required.")
    @Pattern(regexp = "^[A-Za-z0-9-]*$", message = "Doctor Code accepts alphanumeric and - only.")
    private String doctorCode;

    private String doctorName;

    @NotBlank(message = "Reason for consultation is required.")
    @Size(min = 3, max = 250, message = "Reason for consultation is required.")
    @Pattern(regexp = "^[A-Za-z0-9 !@#$%&*(),._?:{}]*$",
            message = "Reason for consultation contains invalid characters")
    private String chiefComplaint;

    private Boolean maternity;

    private BigDecimal runningBill;

    private BigDecimal consultationFee;

    @JsonProperty("coPayPercent")
    private BigDecimal coPayPercent;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate validTo;
}
