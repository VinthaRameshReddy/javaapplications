package com.medgo.loaservice.domain.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserBankDetailDTO {

    // TABLE REFERENCE MEDGO.DBO.BANK
    @NotNull(message = "Bank Id is required.")
    private Long bankId;

    private String memberCode;

    private String accountName;

    @Max(value = 15, message = "Bank account number accepts up to 15 digits length only")
    private String accountNumber;

    // For email notification
    private String bankName;
}
