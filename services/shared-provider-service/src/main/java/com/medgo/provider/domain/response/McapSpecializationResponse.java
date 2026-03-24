package com.medgo.provider.domain.response;

import lombok.Data;

@Data
public class McapSpecializationResponse {
    private String specializationCode;
    private String description;
    private String type;

    // Add constructor with three parameters
    public McapSpecializationResponse(String specializationCode, String description, String type) {
        this.specializationCode = specializationCode;
        this.description = description;
        this.type = type;
    }
}