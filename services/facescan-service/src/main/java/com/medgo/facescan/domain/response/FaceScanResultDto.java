package com.medgo.facescan.domain.response;


import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.Map;

@Data
@AllArgsConstructor
public class FaceScanResultDto {
    //private int status;
    private String message;
    private ScanResult scanResult;

    @Data
    @AllArgsConstructor
    public static class ScanResult {
        private String sessionId;
        private LocalDateTime scanDate;
        private String result;
        private Map<String, Object> scanData; // Store JSON as a map
    }
}

