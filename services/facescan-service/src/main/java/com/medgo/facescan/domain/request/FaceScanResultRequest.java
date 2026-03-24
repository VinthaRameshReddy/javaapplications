package com.medgo.facescan.domain.request;


import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.ToString;

import java.util.Map;

@Data
@ToString
public class FaceScanResultRequest extends BaseDto {

    @NotBlank(message = "Session ID is required")
    private String sessionId;

    @NotBlank(message = "Scan result is required")
    private String scanResult;

    private Map<String, Object> scanData; // JSON Data as Map

    @NotBlank(message = "End time is required")
    private String endTime; // Will be parsed into LocalDateTime
}