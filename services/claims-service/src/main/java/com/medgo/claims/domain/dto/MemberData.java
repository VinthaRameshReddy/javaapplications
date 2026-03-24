package com.medgo.claims.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MemberData {
    private String principalCode;
    private String companyCode; // accountCode from membership service
    private String accountName;
    private LocalDate birthDate;
    private Float age; // Age from membership service response
    private Short memType; // 0=Principal, 1=Qualified Dependent, 2=Extended Dependent
    private String memTypeString; // Original memType string from membership service
}

