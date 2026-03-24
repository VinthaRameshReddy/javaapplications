package com.medgo.facescan.domain.request;


import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FaceScanEligibilityRequest extends BaseDto {

    @NotBlank(message = "Member code is required")
    private String memberCode;
}
