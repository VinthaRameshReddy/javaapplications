package com.medgo.member.domain.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UtilizationLegacyResponse {

    private int status;
    private String message;
    private List<UtilizationResponse> list;

}
