// ...existing code...
package com.medgo.facescan.domain.request;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Map;

class RequestDtoTests {

    @Test
    void faceScanEligibilityRequest_roundtrip() {
        FaceScanEligibilityRequest r = new FaceScanEligibilityRequest("M100");
        assertEquals("M100", r.getMemberCode());
    }

    @Test
    void fetchFaceScanResultRequest_roundtrip() {
        FetchFaceScanResultRequest r = new FetchFaceScanResultRequest();
        r.setSessionId("S1");
        assertEquals("S1", r.getSessionId());
    }

    @Test
    void faceScanTncRequest_and_faceScanResultRequest() {
        FaceScanTncRequest t = new FaceScanTncRequest();
        t.setFedId("F1");
        t.setConsent(true);
        assertTrue(t.isConsent());
        assertEquals("F1", t.getFedId());

        FaceScanResultRequest res = new FaceScanResultRequest();
        res.setSessionId("S2");
        res.setScanResult("SUCCESS");
        res.setScanData(Map.of("k","v"));
        res.setEndTime("2025-11-03T10:00:00");
        assertEquals("S2", res.getSessionId());
        assertEquals("SUCCESS", res.getScanResult());
        assertEquals("v", res.getScanData().get("k"));
    }

    @Test
    void faceScanHistoryRequest_roundtrip() {
        FaceScanHistoryRequest h = new FaceScanHistoryRequest();
        h.setMemberCode("M200");
        assertEquals("M200", h.getMemberCode());
    }
}

