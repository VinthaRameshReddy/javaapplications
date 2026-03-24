package com.medgo.facescan.domain.response;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
public class FaceScanHistoryDto {

    private String message;
    private List<ScanRecord> scanHistory;

    @Data
    @AllArgsConstructor
    public static class ScanRecord {
        private String sessionId;
        private LocalDateTime scanDate;
        private String scanResult;
        private Double healthScore; // ✅ Now sending only healthScore instead of full scanData
        private Double multiplier;
    }
}
