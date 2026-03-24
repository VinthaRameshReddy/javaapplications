package com.medgo.loaservice.domain.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DiagnosisDTO {
    private String diagnosisCode;
    private String diagnosisName;

    private Boolean primary;
    private Boolean toRuleOut;
    private Boolean toConsider;
    private Boolean statusPost;
}
