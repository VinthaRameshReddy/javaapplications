package com.medgo.loaservice.domain.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InPatientDTO {

    @NotBlank(message = "Requesting hospital is required.")
    private String hospitalCode;

    private String doctorCode;

    private String doctorName;

    @NotBlank(message = "Reason for consultation is required.")
    @Size(min = 3, max = 250, message = "Reason for consultation is required.")
    private String chiefComplaint;

    // @NotNull
    @Valid
    private DiagnosisDTO diagnosis;

    // @NotNull
    @Valid
    private ProcedureDTO procedure;
}
