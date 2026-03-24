package com.medgo.loaservice.domain.dto.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
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
public class InquiryDTO {

    private Long serviceTypeId;

    // FROM MEDGO.SERVICE_SUBTYPE
    @NotNull(message = "Medgo Service Subtype id is required.")
    private Long medgoServiceSubtypeId;

    // FROM MEDGO.USER_BANK_DETAIL
    @NotNull(message = "Medgo User bank detail id is required.")
    private Long medgoUserBankDetailId;

    private Long claimNatureId;

    private Long claimTypeId;

    @Pattern(regexp = "^[A-Za-z0-9]*$", message = "Control code accepts alphanumeric only.")
    private String controlCode;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate treatmentDate;

    private BigDecimal totalClaim;

    private String hospitalCode;

    private String doctorCode;

    private String doctorName;
}
