package com.medgo.loaservice.domain.dto.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.Valid;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TestV2DTO {

    @NotNull(message = "Service sub type id is required.")
    private Long serviceSubTypeId;

    @NotNull(message = "Clinic/Hospital where test/procedure will be done is required.")
    private String hospitalCode;

    @JsonFormat(pattern = "yyyy-MM-dd")
    @FutureOrPresent(message = "End of validity is invalid.")
    private LocalDate validTo;

    @NotNull(message = "Running bill is required.")
    @Min(value = 1, message = "Running bill should not be below 1.")
    private BigDecimal runningBill;

    private Boolean coPay;

    private List<PhysicianDTO> physicians;

    private List<DiagnosisModel> diagnosisContainer;

    @Valid
    private List<ProcedureModel> procedureContainer;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PhysicianDTO {
        private String doctorCode;
        private String doctorName;
        private String specializationCode;
        private String specializationName;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DiagnosisModel {
        private List<InnerDiagnosis> diagnoses;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class InnerDiagnosis {
        private String diagnosisCode;
        private String description;
        @Builder.Default
        private Long serviceKeyId = 0L;
        private boolean temporary;
        private List<InnerDiagnosis> child;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ProcedureModel {
        @Valid
        private List<InnerProcedure> procedures;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class InnerProcedure {
        private String procedureCode;
        private String name;
        @Min(value = 1, message = "Procedure amount should not be below 1.")
        private BigDecimal amount;
        private boolean temporary;
    }
}
