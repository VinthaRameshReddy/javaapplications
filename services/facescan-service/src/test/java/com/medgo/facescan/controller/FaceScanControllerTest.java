package com.medgo.facescan.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.medgo.commons.CommonResponse;
import com.medgo.commons.ErrorResponse;
import com.medgo.facescan.domain.request.*;
import com.medgo.facescan.service.FaceScanService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FaceScanControllerTest {

    @Mock
    private FaceScanService faceScanService;

    private FaceScanController controller;

    @BeforeEach
    void setUp() {
        controller = new FaceScanController(faceScanService);
    }

    // checkEligibility
    @Test
    void checkEligibility_success_returnsCommonResponse() {
        FaceScanEligibilityRequest req = new FaceScanEligibilityRequest("M123");
        CommonResponse expected = CommonResponse.success("eligible");
        when(faceScanService.checkEligibility("M123")).thenReturn(expected);

        CommonResponse actual = controller.checkEligibility(req);

        assertNotNull(actual);
        assertEquals("200", actual.getStatusCode());
        assertEquals("SUCCESS", actual.getResponse());
        verify(faceScanService, times(1)).checkEligibility("M123");
    }

    @Test
    void checkEligibility_serviceThrowsRuntime_propagates() {
        FaceScanEligibilityRequest req = new FaceScanEligibilityRequest("X");
        when(faceScanService.checkEligibility("X")).thenThrow(new RuntimeException("boom"));

        RuntimeException ex = assertThrows(RuntimeException.class, () -> controller.checkEligibility(req));
        assertEquals("boom", ex.getMessage());
        verify(faceScanService, times(1)).checkEligibility("X");
    }

    // acceptTncAndStartSession
    @Test
    void acceptTncAndStartSession_success_returnsCommonResponse() {
        FaceScanTncRequest req = new FaceScanTncRequest();
        req.setFedId("FED123");
        req.setConsent(true);

        CommonResponse expected = CommonResponse.success("started");
        when(faceScanService.acceptTncAndInitiateSession(req)).thenReturn(expected);

        CommonResponse actual = controller.acceptTncAndStartSession(req);

        assertNotNull(actual);
        assertEquals("200", actual.getStatusCode());
        verify(faceScanService, times(1)).acceptTncAndInitiateSession(req);
    }

    @Test
    void acceptTncAndStartSession_serviceReturnsError_propagatesErrorResponse() {
        FaceScanTncRequest req = new FaceScanTncRequest();
        req.setFedId("FEDX");
        req.setConsent(true);

        CommonResponse err = CommonResponse.error(new ErrorResponse(400, "BAD", "bad"), 400);
        when(faceScanService.acceptTncAndInitiateSession(req)).thenReturn(err);

        CommonResponse actual = controller.acceptTncAndStartSession(req);

        assertNotNull(actual);
        assertEquals("400", actual.getStatusCode());
        assertEquals("ERROR", actual.getResponse());
        verify(faceScanService, times(1)).acceptTncAndInitiateSession(req);
    }

    // storeFaceScanResult
    @Test
    void storeFaceScanResult_success_returnsCommonResponse() throws JsonProcessingException {
        FaceScanResultRequest req = new FaceScanResultRequest();
        req.setSessionId("S1");
        req.setScanResult("OK");
        req.setEndTime("2025-11-03T10:00:00");

        CommonResponse expected = CommonResponse.success("stored");
        when(faceScanService.storeFaceScanResult(req)).thenReturn(expected);

        CommonResponse actual = controller.storeFaceScanResult(req);

        assertNotNull(actual);
        assertEquals("200", actual.getStatusCode());
        verify(faceScanService, times(1)).storeFaceScanResult(req);
    }

    @Test
    void storeFaceScanResult_serviceThrowsJsonProcessingException_propagates() throws JsonProcessingException {
        FaceScanResultRequest req = new FaceScanResultRequest();
        req.setSessionId("S2");
        req.setScanResult("OK");
        req.setEndTime("2025-11-03T10:00:00");

        when(faceScanService.storeFaceScanResult(req)).thenThrow(new com.fasterxml.jackson.core.JsonProcessingException("bad json"){});

        assertThrows(com.fasterxml.jackson.core.JsonProcessingException.class, () -> controller.storeFaceScanResult(req));
        verify(faceScanService, times(1)).storeFaceScanResult(req);
    }

    // getFaceScanHistory
    @Test
    void getFaceScanHistory_success_returnsCommonResponse() {
        FaceScanHistoryRequest req = new FaceScanHistoryRequest();
        req.setMemberCode("M1");

        CommonResponse expected = CommonResponse.success("history");
        when(faceScanService.getFaceScanHistory(req)).thenReturn(expected);

        CommonResponse actual = controller.getFaceScanHistory(req);

        assertNotNull(actual);
        assertEquals("200", actual.getStatusCode());
        verify(faceScanService, times(1)).getFaceScanHistory(req);
    }

    @Test
    void getFaceScanHistory_serviceThrowsRuntime_propagates() {
        FaceScanHistoryRequest req = new FaceScanHistoryRequest();
        req.setMemberCode("Z");
        when(faceScanService.getFaceScanHistory(req)).thenThrow(new RuntimeException("fail"));

        RuntimeException ex = assertThrows(RuntimeException.class, () -> controller.getFaceScanHistory(req));
        assertEquals("fail", ex.getMessage());
        verify(faceScanService, times(1)).getFaceScanHistory(req);
    }

    // fetchFaceScanResult
    @Test
    void fetchFaceScanResult_success_returnsCommonResponse() {
        FetchFaceScanResultRequest req = new FetchFaceScanResultRequest();
        req.setSessionId("S100");

        CommonResponse expected = CommonResponse.success("result");
        when(faceScanService.fetchFaceScanResult("S100")).thenReturn(expected);

        CommonResponse actual = controller.fetchFaceScanResult(req);

        assertNotNull(actual);
        assertEquals("200", actual.getStatusCode());
        verify(faceScanService, times(1)).fetchFaceScanResult("S100");
    }

    @Test
    void fetchFaceScanResult_serviceReturnsError_propagatesError() {
        FetchFaceScanResultRequest req = new FetchFaceScanResultRequest();
        req.setSessionId("S200");

        CommonResponse err = CommonResponse.error(new ErrorResponse(404, "NOT", "not"), 404);
        when(faceScanService.fetchFaceScanResult("S200")).thenReturn(err);

        CommonResponse actual = controller.fetchFaceScanResult(req);

        assertNotNull(actual);
        assertEquals("404", actual.getStatusCode());
        assertEquals("ERROR", actual.getResponse());
        verify(faceScanService, times(1)).fetchFaceScanResult("S200");
    }

    // getJsonDataTemplate
//    @Test
//    void getJsonDataTemplate_readsResource_returnsContent() throws IOException {
//        String content = controller.getJsonDataTemplate();
//        assertNotNull(content);
//        assertTrue(content.contains("master"));
//    }

    // annotation presence checks
    @Test
    void methodsHaveEncryptResponseAnnotation() throws NoSuchMethodException {
        Method m1 = FaceScanController.class.getMethod("checkEligibility", FaceScanEligibilityRequest.class);
        Method m2 = FaceScanController.class.getMethod("acceptTncAndStartSession", FaceScanTncRequest.class);
        Method m3 = FaceScanController.class.getMethod("storeFaceScanResult", FaceScanResultRequest.class);
        Method m4 = FaceScanController.class.getMethod("getFaceScanHistory", FaceScanHistoryRequest.class);
        Method m5 = FaceScanController.class.getMethod("fetchFaceScanResult", FetchFaceScanResultRequest.class);

        assertTrue(m1.isAnnotationPresent(com.medgo.crypto.annotation.EncryptResponse.class));
        assertTrue(m2.isAnnotationPresent(com.medgo.crypto.annotation.EncryptResponse.class));
        assertTrue(m3.isAnnotationPresent(com.medgo.crypto.annotation.EncryptResponse.class));
        assertTrue(m4.isAnnotationPresent(com.medgo.crypto.annotation.EncryptResponse.class));
        assertTrue(m5.isAnnotationPresent(com.medgo.crypto.annotation.EncryptResponse.class));
    }
}
