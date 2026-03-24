package com.medgo.claims.domain.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReimbursementDetailsRequest {
    @NotBlank(message = "Control code is required")
    private String controlCode;
    
    @NotBlank(message = "Status is required")
    private String status;
    
    private String entryCode;
}

