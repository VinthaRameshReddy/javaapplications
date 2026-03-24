// ...existing code...
package com.medgo.facescan.domain.response;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class DtoSimpleTests {

    @Test
    void faceScanDto_gettersSetters() {
        FaceScanDto dto = new FaceScanDto("msg", "FED1", Map.of("k","v"), Map.of("f","v"));
        assertEquals("msg", dto.getMessage());
        assertEquals("FED1", dto.getFederatedMembershipCode());
        assertEquals("v", dto.getLastScanData().get("k"));
    }

    @Test
    void faceScanResultDto_and_scanResult() {
        FaceScanResultDto.ScanResult sr = new FaceScanResultDto.ScanResult("S1", LocalDateTime.now(), "SUCCESS", Map.of("k","v"));
        FaceScanResultDto dto = new FaceScanResultDto("ok", sr);
        assertEquals("ok", dto.getMessage());
        assertEquals("S1", dto.getScanResult().getSessionId());
    }

    @Test
    void faceScanHistoryDto_and_record() {
        FaceScanHistoryDto.ScanRecord r = new FaceScanHistoryDto.ScanRecord("S2", LocalDateTime.now(), "SUCCESS", 99.9, 1.2);
        FaceScanHistoryDto dto = new FaceScanHistoryDto("history", List.of(r));
        assertEquals(1, dto.getScanHistory().size());
        assertEquals(99.9, dto.getScanHistory().get(0).getHealthScore());
    }
}

