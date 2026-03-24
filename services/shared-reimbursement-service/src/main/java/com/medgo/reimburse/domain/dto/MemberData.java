package com.medgo.reimburse.domain.dto;

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
    private String companyCode;
    private String accountName;
    private LocalDate birthDate;
    private Float age;
    private Short memType;
    private String memTypeString;
}
