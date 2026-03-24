package com.medgo.facescan.domain.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;



@Data
@NoArgsConstructor
@AllArgsConstructor
public class FetchFaceScanResultRequest {

    @NotBlank(message = "Session ID is required")
    private String sessionId;
}
