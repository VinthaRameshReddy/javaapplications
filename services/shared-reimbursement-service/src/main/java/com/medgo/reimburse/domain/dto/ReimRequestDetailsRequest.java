package com.medgo.reimburse.domain.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ReimRequestDetailsRequest {
    @NotBlank(message = "Control code is required")
    private String controlCode;
    
    @NotBlank(message = "Status is required")
    private String status;
    
    private String entryCode;
}

