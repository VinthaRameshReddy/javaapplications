package com.medgo.loaservice.domain.dto.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
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
public class ConsultationDTO {

    @NotNull(message = "Service sub type id is required.")
    private Long serviceSubTypeId;

    @NotBlank(message = "Requesting hospital is required.")
    private String hospitalCode;

    private String doctorCode;
    private String doctorName;
    private String doctorPrcNo;

    @NotBlank(message = "Reason for consultation is required.")
    @Size(min = 3, max = 250, message = "Reason for consultation is required.")
    private String chiefComplaint;

    private Boolean maternity;

    @NotNull(message = "Running bill is required.")
    @Min(value = 1, message = "Running bill should not be below 1.")
    private BigDecimal runningBill;

    @NotNull(message = "Consultation Fee is required.")
    @Min(value = 1, message = "Consultation Fee should not be below 1.")
    private BigDecimal consultationFee;

    private Boolean coPay;

    @JsonFormat(pattern = "yyyy-MM-dd")
    @FutureOrPresent(message = "End of validity is invalid.")
    private LocalDate validTo;
}
