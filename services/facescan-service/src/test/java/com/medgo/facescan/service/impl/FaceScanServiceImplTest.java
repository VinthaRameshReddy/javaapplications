package com.medgo.facescan.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.medgo.commons.CommonResponse;
import com.medgo.commons.ErrorResponse;
import com.medgo.facescan.components.MedGoCustomSecurityContext;
import com.medgo.facescan.domain.models.medgo.FaceScanSessionModel;
import com.medgo.facescan.domain.models.medgo.StoreFaceScanResultModel;
import com.medgo.facescan.domain.models.membership.MembershipModel;
import com.medgo.facescan.domain.request.FaceScanHistoryRequest;
import com.medgo.facescan.domain.request.FaceScanResultRequest;
import com.medgo.facescan.domain.request.FaceScanTncRequest;
import com.medgo.facescan.repository.medgo.FaceScanMemberWhiteListedRepository;
import com.medgo.facescan.repository.medgo.FaceScanResultRepository;
import com.medgo.facescan.repository.medgo.FaceScanSessionRepository;
import com.medgo.facescan.repository.medgo.FaceScanWhiteListedRepository;
import com.medgo.facescan.repository.membership.MembershipModelRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.InvalidDataAccessResourceUsageException;

import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FaceScanServiceImplTest {

    @Mock
    private FaceScanSessionRepository sessionRepository;
    @Mock
    private FaceScanResultRepository faceScanResultRepository;
    @Mock
    private FaceScanWhiteListedRepository faceScanWhiteListedRepository;
    @Mock
    private FaceScanMemberWhiteListedRepository faceScanMemberWhiteListedRepository;
    @Mock
    private MembershipModelRepository membershipRepository;
    @Mock
    private MedGoCustomSecurityContext securityContext;

    private ObjectMapper objectMapper;

    private FaceScanServiceImpl service;

    @Captor
    private ArgumentCaptor<FaceScanSessionModel> sessionCaptor;

    @Captor
    private ArgumentCaptor<StoreFaceScanResultModel> scanCaptor;

    @BeforeEach
    void setUp() {
        // use a real mapper for most tests
        objectMapper = new ObjectMapper();
        service = new FaceScanServiceImpl(sessionRepository, faceScanResultRepository, objectMapper,
                faceScanWhiteListedRepository, faceScanMemberWhiteListedRepository, membershipRepository, securityContext);

        // Default mock behavior for securityContext - return valid values for most tests
        // Use lenient() to avoid UnnecessaryStubbingException for tests that don't use these mocks
        org.mockito.Mockito.lenient().when(securityContext.authenticatedUsername()).thenReturn("testUser");
        // Default memberCode - individual tests can override this
        org.mockito.Mockito.lenient().when(securityContext.authenticatedMemberCode()).thenReturn("DEFAULT_MEMBER");
    }
    
    // Helper method to setup validateMemCode mock behavior
    private void setupValidMemberCode(String memberCode, MembershipModel membership) {
        when(securityContext.authenticatedMemberCode()).thenReturn(memberCode);
        when(membershipRepository.findByMemberCode(memberCode)).thenReturn(Optional.of(membership));
    }

    // --- checkEligibility tests ---
    @Test
    void checkEligibility_nullOrEmptyMemberCode_returns400() {
        CommonResponse resp1 = service.checkEligibility(null);
        assertEquals("400", resp1.getStatusCode());
        assertEquals(400, ((ErrorResponse)resp1.getData()).statusCode());

        CommonResponse resp2 = service.checkEligibility("");
        assertEquals("400", resp2.getStatusCode());
        assertEquals(400, ((ErrorResponse)resp2.getData()).statusCode());
    }

    @Test
    void checkEligibility_memberNotFound_returns404() {
        when(securityContext.authenticatedMemberCode()).thenReturn("M1");
        when(membershipRepository.findByMemberCode("M1")).thenReturn(Optional.empty());
        CommonResponse resp = service.checkEligibility("M1");
        assertEquals("403", resp.getStatusCode());
        assertEquals(403, ((ErrorResponse)resp.getData()).statusCode());
    }

    @Test
    void checkEligibility_notCorporate_returns100() {
        MembershipModel m = new MembershipModel();
        m.setMemberCode("M2");
        m.setAccountCode("A1");
        m.setMemType("I");
        setupValidMemberCode("M2", m);
        when(faceScanMemberWhiteListedRepository.findActiveMember(eq("M2"), anyChar(), any())).thenReturn(0);
        when(faceScanWhiteListedRepository.findActiveMember(eq("A1"), anyChar(), any())).thenReturn(0);

        CommonResponse resp = service.checkEligibility("M2");
        assertEquals("100", resp.getStatusCode());
        assertEquals(100, ((ErrorResponse)resp.getData()).statusCode());
    }

    @Test
    void checkEligibility_cooldownNotOver_returns102() {
        MembershipModel m = new MembershipModel();
        m.setMemberCode("M3");
        m.setAccountCode("A3");
        m.setMemType("C");
        setupValidMemberCode("M3", m);
        when(faceScanMemberWhiteListedRepository.findActiveMember(eq("M3"), anyChar(), any())).thenReturn(1);

        FaceScanSessionModel last = new FaceScanSessionModel("FED123", "M3");
        last.setUpdatedOn(LocalDateTime.now()); // recent -> cooldown not over (default is 7 days)
        when(sessionRepository.findTop1ByMemberCode(eq("M3"), any(org.springframework.data.domain.Pageable.class))).thenReturn(Collections.singletonList(last));

        CommonResponse resp = service.checkEligibility("M3");
        assertEquals("102", resp.getStatusCode());
        assertEquals(102, ((ErrorResponse)resp.getData()).statusCode());
    }

    @Test
    void checkEligibility_success_createsOrUpdatesSession_returnsSuccess() {
        MembershipModel m = new MembershipModel();
        m.setMemberCode("M4");
        m.setAccountCode("A4");
        m.setMemType("C");
        setupValidMemberCode("M4", m);
        when(faceScanMemberWhiteListedRepository.findActiveMember(eq("M4"), anyChar(), any())).thenReturn(1);
        when(sessionRepository.findTop1ByMemberCode(eq("M4"), any(org.springframework.data.domain.Pageable.class))).thenReturn(Collections.emptyList());
        when(faceScanResultRepository.findLatestScanByMemberCode("M4")).thenReturn(Collections.emptyList());

        CommonResponse resp = service.checkEligibility("M4");
        assertEquals("200", resp.getStatusCode());
        assertNotNull(resp.getData());
        verify(sessionRepository, times(1)).save(sessionCaptor.capture());
        FaceScanSessionModel saved = sessionCaptor.getValue();
        assertEquals("M4", saved.getMemberCode());
        assertNotNull(saved.getFedId());
    }

//    @Test
//    void checkEligibility_dbException_returns500() {
//        when(securityContext.authenticatedUsername()).thenReturn("testUser");
//        when(securityContext.authenticatedMemberCode()).thenReturn("X");
//        // validateMemCode will call findByMemberCode, throw exception there
//        when(membershipRepository.findByMemberCode("X")).thenThrow(new InvalidDataAccessResourceUsageException("Database error"));
//        CommonResponse resp = service.checkEligibility("X");
//        assertEquals("500", resp.getStatusCode());
//        assertEquals(500, ((ErrorResponse)resp.getData()).statusCode());
//    }

    @Test
    void checkEligibility_withLastScan_parsesLastScanData() throws Exception {
        MembershipModel m = new MembershipModel();
        m.setMemberCode("MLast");
        m.setAccountCode("A20");
        m.setMemType("C");
        setupValidMemberCode("MLast", m);
        when(faceScanMemberWhiteListedRepository.findActiveMember(eq("MLast"), anyChar(), any())).thenReturn(1);
        when(sessionRepository.findTop1ByMemberCode(eq("MLast"), any(org.springframework.data.domain.Pageable.class))).thenReturn(Collections.emptyList());

        StoreFaceScanResultModel lastScan = new StoreFaceScanResultModel();
        lastScan.setSessionId("SLast");
        Map<String,Object> scanData = new HashMap<>();
        scanData.put("foo","bar");
        lastScan.setScanData(objectMapper.writeValueAsString(scanData));
        when(faceScanResultRepository.findLatestScanByMemberCode("MLast")).thenReturn(List.of(lastScan));

        CommonResponse resp = service.checkEligibility("MLast");
        assertEquals("200", resp.getStatusCode());
        Object data = resp.getData();
        assertNotNull(data);
        assertInstanceOf(com.medgo.facescan.domain.response.FaceScanDto.class, data);
        com.medgo.facescan.domain.response.FaceScanDto dto = (com.medgo.facescan.domain.response.FaceScanDto) data;
        assertNotNull(dto.getLastScanData());
        Map<String,Object> last = (Map<String,Object>) dto.getLastScanData(); // Redundant cast removed
        assertEquals("bar", last.get("foo"));
    }

    // --- acceptTncAndInitiateSession tests ---
    @Test
    void acceptTnc_missingFedId_returns400() {
        FaceScanTncRequest req = new FaceScanTncRequest();
        req.setFedId(null);
        req.setConsent(true);

        CommonResponse resp = service.acceptTncAndInitiateSession(req);
        assertEquals("400", resp.getStatusCode());
        assertEquals(400, ((ErrorResponse)resp.getData()).statusCode());
    }

    @Test
    void acceptTnc_consentFalse_returns400() {
        FaceScanTncRequest req = new FaceScanTncRequest();
        req.setFedId("F1");
        req.setConsent(false);

        CommonResponse resp = service.acceptTncAndInitiateSession(req);
        assertEquals("400", resp.getStatusCode());
        assertEquals(400, ((ErrorResponse)resp.getData()).statusCode());
    }

    @Test
    void acceptTnc_sessionNotFound_returns404() {
        // Note: validateMemCode won't be called if session is not found first
        FaceScanTncRequest req = new FaceScanTncRequest();
        req.setFedId("F2");
        req.setConsent(true);
        when(sessionRepository.findByFedId("F2")).thenReturn(Optional.empty());

        CommonResponse resp = service.acceptTncAndInitiateSession(req);
        assertEquals("404", resp.getStatusCode());
        assertEquals(404, ((ErrorResponse)resp.getData()).statusCode());
    }

    @Test
    void acceptTnc_success_savesAndReturnsSession() {
        when(securityContext.authenticatedMemberCode()).thenReturn("M5");
        MembershipModel m = new MembershipModel();
        m.setMemberCode("M5");
        m.setAccountCode("A5");
        m.setMemType("C");
        when(membershipRepository.findByMemberCode("M5")).thenReturn(Optional.of(m));
        
        FaceScanTncRequest req = new FaceScanTncRequest();
        req.setFedId("F3");
        req.setConsent(true);

        FaceScanSessionModel existing = new FaceScanSessionModel("F3", "M5");
        existing.setId(10L);
        when(sessionRepository.findByFedId("F3")).thenReturn(Optional.of(existing));

        CommonResponse resp = service.acceptTncAndInitiateSession(req);
        assertEquals("200", resp.getStatusCode());
        verify(sessionRepository, times(1)).save(any(FaceScanSessionModel.class));
        verify(sessionRepository, times(1)).save(sessionCaptor.capture());
        FaceScanSessionModel saved = sessionCaptor.getValue();
        assertEquals("STARTED", saved.getStatus());
        assertNotNull(saved.getSessionId());
    }

    @Test
    void acceptTnc_dbException_returns500() {
        FaceScanTncRequest req = new FaceScanTncRequest();
        req.setFedId("F4");
        req.setConsent(true);
        when(sessionRepository.findByFedId("F4")).thenThrow(new InvalidDataAccessResourceUsageException("Database error"));
        // Exception thrown before validateMemCode is called, so default lenient mock in setUp is sufficient

        CommonResponse resp = service.acceptTncAndInitiateSession(req);
        assertEquals("500", resp.getStatusCode());
        assertEquals(500, ((ErrorResponse)resp.getData()).statusCode());
    }

    @Test
    void acceptTnc_withOptionalFields_setsFields() {
        when(securityContext.authenticatedMemberCode()).thenReturn("MOpt");
        MembershipModel m = new MembershipModel();
        m.setMemberCode("MOpt");
        m.setAccountCode("AOpt");
        m.setMemType("C");
        when(membershipRepository.findByMemberCode("MOpt")).thenReturn(Optional.of(m));
        
        FaceScanTncRequest req = new FaceScanTncRequest();
        req.setFedId("FOpt");
        req.setConsent(true);
        req.setMemberName("John Doe");
        req.setAge(30);
        req.setGender("M");
        req.setWeight(75.5);
        req.setHeight(180.2);
        req.setIsSmoker("true");
        req.setIsDiabetic("false");
        req.setBpMedication("Y");

        FaceScanSessionModel existing = new FaceScanSessionModel("FOpt", "MOpt");
        existing.setId(99L);
        when(sessionRepository.findByFedId("FOpt")).thenReturn(Optional.of(existing));

        CommonResponse resp = service.acceptTncAndInitiateSession(req);
        assertEquals("200", resp.getStatusCode());
        verify(sessionRepository, times(1)).save(sessionCaptor.capture());
        FaceScanSessionModel saved = sessionCaptor.getValue();
        assertEquals("John Doe", saved.getMemberName());
        assertEquals(30, saved.getAge());
        assertEquals("M", saved.getGender());
        assertEquals(75.5, saved.getWeight());
        assertEquals(180.2, saved.getHeight());
        assertEquals("true", saved.getIsSmoker());
        assertEquals("false", saved.getIsDiabetic());
        assertEquals("Y", saved.getBpMedication());
    }

    // --- storeFaceScanResult tests ---
    @Test
    void storeFaceScanResult_missingSessionId_returns400() {
        FaceScanResultRequest req = new FaceScanResultRequest();
        req.setSessionId(null);
        CommonResponse resp = service.storeFaceScanResult(req);
        assertEquals("400", resp.getStatusCode());
        assertEquals(400, ((ErrorResponse)resp.getData()).statusCode());
    }

    @Test
    void storeFaceScanResult_sessionNotFound_returns404() {
        // Note: validateMemCode won't be called if session is not found first
        FaceScanResultRequest req = new FaceScanResultRequest();
        req.setSessionId("S1");
        when(sessionRepository.findBySessionId("S1")).thenReturn(Optional.empty());

        CommonResponse resp = service.storeFaceScanResult(req);
        assertEquals("404", resp.getStatusCode());
        assertEquals(404, ((ErrorResponse)resp.getData()).statusCode());
    }

    @Test
    void storeFaceScanResult_invalidEndTime_returns400() {
        when(securityContext.authenticatedMemberCode()).thenReturn("M");
        MembershipModel m = new MembershipModel();
        m.setMemberCode("M");
        m.setAccountCode("A");
        m.setMemType("C");
        when(membershipRepository.findByMemberCode("M")).thenReturn(Optional.of(m));
        
        FaceScanResultRequest req = new FaceScanResultRequest();
        req.setSessionId("S2");
        FaceScanSessionModel session = new FaceScanSessionModel("F", "M");
        when(sessionRepository.findBySessionId("S2")).thenReturn(Optional.of(session));
        req.setEndTime("not-a-date");

        CommonResponse resp = service.storeFaceScanResult(req);
        assertEquals("400", resp.getStatusCode());
        assertEquals(400, ((ErrorResponse)resp.getData()).statusCode());
    }
//
//    @Test
//    void storeFaceScanResult_serializationFailure_returns500() throws JsonProcessingException {
//        FaceScanResultRequest req = new FaceScanResultRequest();
//        req.setSessionId("S3");
//        req.setEndTime("2025-11-03T10:00:00");
//        req.setScanData(Map.of("k","v"));
//        when(sessionRepository.findBySessionId("S3")).thenReturn(Optional.of(new FaceScanSessionModel("F", "M")));
//
//        // create a service instance with a mock ObjectMapper that throws
//        ObjectMapper mockMapper = mock(ObjectMapper.class);
//        when(mockMapper.writeValueAsString(any())).thenThrow(new JsonProcessingException("bad"){});
//        FaceScanServiceImpl svcWithBadMapper = new FaceScanServiceImpl(sessionRepository, faceScanResultRepository, mockMapper,
//                faceScanWhiteListedRepository, faceScanMemberWhiteListedRepository, membershipRepository);
//
//        CommonResponse resp = svcWithBadMapper.storeFaceScanResult(req);
//        assertEquals("001", resp.getStatusCode());
//        assertEquals(500, ((ErrorResponse)resp.getData()).statusCode());
//    }

    @Test
    void storeFaceScanResult_success_savesAndReturns() {
        when(securityContext.authenticatedMemberCode()).thenReturn("M6");
        MembershipModel m = new MembershipModel();
        m.setMemberCode("M6");
        m.setAccountCode("A6");
        m.setMemType("C");
        when(membershipRepository.findByMemberCode("M6")).thenReturn(Optional.of(m));
        
        FaceScanResultRequest req = new FaceScanResultRequest();
        req.setSessionId("S4");
        req.setEndTime("2025-11-03T10:00:00");
        req.setScanData(Map.of("measurementID","m1"));
        FaceScanSessionModel session = new FaceScanSessionModel("F4","M6");
        session.setId(5L);
        when(sessionRepository.findBySessionId("S4")).thenReturn(Optional.of(session));
        when(faceScanResultRepository.findBySessionId("S4")).thenReturn(Optional.empty());

        CommonResponse resp = service.storeFaceScanResult(req);
        assertEquals("200", resp.getStatusCode());
        verify(faceScanResultRepository, times(1)).save(any(StoreFaceScanResultModel.class));
        verify(sessionRepository, times(1)).save(any(FaceScanSessionModel.class));
    }

    @Test
    void storeFaceScanResult_existingScan_updatesMeasurementId() {
        when(securityContext.authenticatedMemberCode()).thenReturn("M5");
        MembershipModel m = new MembershipModel();
        m.setMemberCode("M5");
        m.setAccountCode("A5");
        m.setMemType("C");
        when(membershipRepository.findByMemberCode("M5")).thenReturn(Optional.of(m));
        
        FaceScanResultRequest req = new FaceScanResultRequest();
        req.setSessionId("S5");
        req.setEndTime("2025-11-03T11:00:00");
        req.setScanData(Map.of("measurementID","m2"));

        FaceScanSessionModel session = new FaceScanSessionModel("F5","M5");
        session.setId(50L);
        when(sessionRepository.findBySessionId("S5")).thenReturn(Optional.of(session));

        StoreFaceScanResultModel existing = new StoreFaceScanResultModel();
        existing.setSessionId("S5");
        existing.setMeasurementID("old");
        when(faceScanResultRepository.findBySessionId("S5")).thenReturn(Optional.of(existing));

        CommonResponse resp = service.storeFaceScanResult(req);
        assertEquals("200", resp.getStatusCode());
        verify(faceScanResultRepository, times(1)).save(scanCaptor.capture());
        StoreFaceScanResultModel saved = scanCaptor.getValue();
        assertEquals("m2", saved.getMeasurementID());
        verify(sessionRepository, times(1)).save(any(FaceScanSessionModel.class));
    }

    @Test
    void storeFaceScanResult_dbException_returns500() {
        FaceScanResultRequest req = new FaceScanResultRequest();
        req.setSessionId("SDB");
        when(sessionRepository.findBySessionId("SDB")).thenThrow(new InvalidDataAccessResourceUsageException("Database error"));

        CommonResponse resp = service.storeFaceScanResult(req);
        assertEquals("500", resp.getStatusCode());
        assertEquals(500, ((ErrorResponse)resp.getData()).statusCode());
    }

    @Test
    void storeFaceScanResult_withNullScanData_serializesNullAndSaves() {
        when(securityContext.authenticatedMemberCode()).thenReturn("MNULL");
        MembershipModel m = new MembershipModel();
        m.setMemberCode("MNULL");
        m.setAccountCode("ANULL");
        m.setMemType("C");
        when(membershipRepository.findByMemberCode("MNULL")).thenReturn(Optional.of(m));
        
        FaceScanResultRequest req = new FaceScanResultRequest();
        req.setSessionId("SNULL");
        req.setEndTime("2025-11-03T12:00:00");
        req.setScanData(null); // explicit null

        FaceScanSessionModel session = new FaceScanSessionModel("FNULL","MNULL");
        session.setId(101L);
        when(sessionRepository.findBySessionId("SNULL")).thenReturn(Optional.of(session));
        when(faceScanResultRepository.findBySessionId("SNULL")).thenReturn(Optional.empty());

        CommonResponse resp = service.storeFaceScanResult(req);
        assertEquals("200", resp.getStatusCode());
        verify(faceScanResultRepository, times(1)).save(scanCaptor.capture());
        StoreFaceScanResultModel saved = scanCaptor.getValue();
        // when ObjectMapper serializes null it produces the string "null"
        assertEquals("null", saved.getScanData());
    }

    @Test
    void fetchFaceScanResult_withNullStoredScanData_returnsDtoWithNullScanData() {
        StoreFaceScanResultModel model = new StoreFaceScanResultModel();
        model.setSessionId("SNULLF");
        model.setEndTime(LocalDateTime.now());
        model.setScanResult("SUCCESS");
        model.setScanData(null);

        when(faceScanResultRepository.findBySessionId("SNULLF")).thenReturn(Optional.of(model));
        // No security context needed for fetchFaceScanResult

        CommonResponse resp = service.fetchFaceScanResult("SNULLF");
        assertEquals("200", resp.getStatusCode());
        com.medgo.facescan.domain.response.FaceScanResultDto dto = (com.medgo.facescan.domain.response.FaceScanResultDto) resp.getData();
        assertNotNull(dto.getScanResult());
        assertNull(dto.getScanResult().getScanData());
    }

    @Test
    void parseScanRecord_picksFirstHealthScoreValue() throws Exception {
        when(securityContext.authenticatedMemberCode()).thenReturn("MHS");
        MembershipModel m = new MembershipModel();
        m.setMemberCode("MHS");
        m.setAccountCode("AHS");
        m.setMemType("C");
        when(membershipRepository.findByMemberCode("MHS")).thenReturn(Optional.of(m));
        FaceScanHistoryRequest req = new FaceScanHistoryRequest();
        req.setMemberCode("MHS");
        StoreFaceScanResultModel model = new StoreFaceScanResultModel();
        model.setSessionId("SHS");
        model.setEndTime(LocalDateTime.now());
        model.setScanResult("SUCCESS");
        Map<String,Object> scanData = new HashMap<>();
        Map<String,Object> channels = new HashMap<>();
        Map<String,Object> hs = new HashMap<>();
        hs.put("dataList", List.of(11.1, 22.2, 33.3));
        channels.put("HEALTH_SCORE", hs);
        scanData.put("channels", channels);
        model.setScanData(objectMapper.writeValueAsString(scanData));
        when(faceScanResultRepository.findByMemberCodeAndScanResultOrderByEndTimeDesc("MHS","SUCCESS")).thenReturn(List.of(model));
        CommonResponse resp = service.getFaceScanHistory(req);
        assertEquals("200", resp.getStatusCode());
        com.medgo.facescan.domain.response.FaceScanHistoryDto dto = (com.medgo.facescan.domain.response.FaceScanHistoryDto) resp.getData();
        assertNotNull(dto.getScanHistory());
        assertEquals(11.1, dto.getScanHistory().get(0).getHealthScore()); // Can be replaced with getFirst(), but keeping for clarity
    }

//    @Test
//    void checkEligibility_unexpectedException_returns500() throws Exception {
//        // create a service with a mapper that throws when reading scan data
//        ObjectMapper badMapper = mock(ObjectMapper.class);
//        when(badMapper.readValue(anyString(), eq(Map.class))).thenThrow(new com.fasterxml.jackson.core.JsonProcessingException("bad"){});
//        FaceScanServiceImpl svc = new FaceScanServiceImpl(sessionRepository, faceScanResultRepository, badMapper,
//                faceScanWhiteListedRepository, faceScanMemberWhiteListedRepository, membershipRepository);
//
//        MembershipModel m = new MembershipModel();
//        m.setMemberCode("MErr");
//        m.setAccountCode("AERR");
//        m.setMemType("C");
//        when(membershipRepository.findByMemberCode("MErr")).thenReturn(Optional.of(m));
//        when(faceScanMemberWhiteListedRepository.findActiveMember(eq("MErr"), anyChar(), any())).thenReturn(1);
//        when(sessionRepository.findTop1ByMemberCode(eq("MErr"), any(PageRequest.class))).thenReturn(Collections.emptyList());
//
//        StoreFaceScanResultModel lastScan = new StoreFaceScanResultModel();
//        lastScan.setScanData("{\"k\":\"v\"}");
//        when(faceScanResultRepository.findLatestScanByMemberCode("MErr")).thenReturn(List.of(lastScan));
//
//        CommonResponse resp = svc.checkEligibility("MErr");
//        assertEquals("001", resp.getStatusCode());
//        assertEquals(500, ((ErrorResponse)resp.getData()).statusCode());
//    }

    @Test
    void acceptTnc_unexpectedException_returns500() {
        when(securityContext.authenticatedMemberCode()).thenReturn("MX");
        MembershipModel m = new MembershipModel();
        m.setMemberCode("MX");
        m.setAccountCode("AX");
        m.setMemType("C");
        when(membershipRepository.findByMemberCode("MX")).thenReturn(Optional.of(m));
        
        FaceScanTncRequest req = new FaceScanTncRequest();
        req.setFedId("FEX");
        req.setConsent(true);

        FaceScanSessionModel existing = new FaceScanSessionModel("FEX", "MX");
        existing.setId(77L);
        when(sessionRepository.findByFedId("FEX")).thenReturn(Optional.of(existing));
        // make save throw a runtime exception (not DataAccessException)
        when(sessionRepository.save(any(FaceScanSessionModel.class))).thenThrow(new RuntimeException("boom-save"));

        CommonResponse resp = service.acceptTncAndInitiateSession(req);
        assertEquals("500", resp.getStatusCode());
        assertEquals(500, ((ErrorResponse)resp.getData()).statusCode());
    }

    @Test
    void storeFaceScanResult_unexpectedException_returns500() {
        when(securityContext.authenticatedMemberCode()).thenReturn("Mdb");
        MembershipModel m = new MembershipModel();
        m.setMemberCode("Mdb");
        m.setAccountCode("Adb");
        m.setMemType("C");
        when(membershipRepository.findByMemberCode("Mdb")).thenReturn(Optional.of(m));
        
        FaceScanResultRequest req = new FaceScanResultRequest();
        req.setSessionId("SDB2");
        req.setEndTime("2025-11-03T10:00:00");
        req.setScanData(Map.of("k","v"));

        FaceScanSessionModel session = new FaceScanSessionModel("Fdb","Mdb");
        when(sessionRepository.findBySessionId("SDB2")).thenReturn(Optional.of(session));
        // make repository.save throw a runtime exception
        when(faceScanResultRepository.findBySessionId("SDB2")).thenReturn(Optional.empty());
        when(faceScanResultRepository.save(any(StoreFaceScanResultModel.class))).thenThrow(new RuntimeException("boom-save"));

        CommonResponse resp = service.storeFaceScanResult(req);
        assertEquals("500", resp.getStatusCode());
        assertEquals(500, ((ErrorResponse)resp.getData()).statusCode());
    }

    @Test
    void getFaceScanHistory_unexpectedException_returns500() {
        when(securityContext.authenticatedMemberCode()).thenReturn("MERRH");
        MembershipModel m = new MembershipModel();
        m.setMemberCode("MERRH");
        m.setAccountCode("AERRH");
        m.setMemType("C");
        when(membershipRepository.findByMemberCode("MERRH")).thenReturn(Optional.of(m));
        
        FaceScanHistoryRequest req = new FaceScanHistoryRequest();
        req.setMemberCode("MERRH");
        when(faceScanResultRepository.findByMemberCodeAndScanResultOrderByEndTimeDesc("MERRH","SUCCESS")).thenThrow(new RuntimeException("boom"));

        CommonResponse resp = service.getFaceScanHistory(req);
        assertEquals("500", resp.getStatusCode());
        assertEquals(500, ((ErrorResponse)resp.getData()).statusCode());
    }

    @Test
    void fetchFaceScanResult_unexpectedException_returns500() {
        when(faceScanResultRepository.findBySessionId("SERR")).thenThrow(new RuntimeException("boom"));
        CommonResponse resp = service.fetchFaceScanResult("SERR");
        assertEquals("500", resp.getStatusCode());
        assertEquals(500, ((ErrorResponse)resp.getData()).statusCode());
    }

    @Test
    void checkEligibility_lastSession_cooldownOver_allowsNewScan() {
        MembershipModel m = new MembershipModel();
        m.setMemberCode("MCOOL");
        m.setAccountCode("ACOOL");
        m.setMemType("C");
        setupValidMemberCode("MCOOL", m);
        when(faceScanMemberWhiteListedRepository.findActiveMember(eq("MCOOL"), anyChar(), any())).thenReturn(1);

        FaceScanSessionModel last = new FaceScanSessionModel("FEDOLD", "MCOOL");
        // set updatedOn far in the past so cooldown is over (default is 7 days)
        last.setUpdatedOn(LocalDateTime.now().minusDays(30));
        last.setId(555L);
        last.setHeight(170.0);
        last.setWeight(70.0);
        when(sessionRepository.findTop1ByMemberCode(eq("MCOOL"), any(org.springframework.data.domain.Pageable.class))).thenReturn(Collections.singletonList(last));
        when(faceScanResultRepository.findLatestScanByMemberCode("MCOOL")).thenReturn(Collections.emptyList());

        CommonResponse resp = service.checkEligibility("MCOOL");
        assertEquals("200", resp.getStatusCode());
        verify(sessionRepository, times(1)).save(sessionCaptor.capture());
        FaceScanSessionModel saved = sessionCaptor.getValue();
        assertEquals(555L, saved.getId());
        assertEquals(170.0, saved.getHeight());
        assertEquals(70.0, saved.getWeight());
    }

    @Test
    void checkEligibility_generatesFedId_whenNoneExists() {
        MembershipModel m = new MembershipModel();
        m.setMemberCode("MGEN");
        m.setAccountCode("AGEN");
        m.setMemType("C");
        setupValidMemberCode("MGEN", m);
        when(faceScanMemberWhiteListedRepository.findActiveMember(eq("MGEN"), anyChar(), any())).thenReturn(1);
        when(sessionRepository.findTop1ByMemberCode(eq("MGEN"), any(org.springframework.data.domain.Pageable.class))).thenReturn(Collections.emptyList());
        when(faceScanResultRepository.findLatestScanByMemberCode("MGEN")).thenReturn(Collections.emptyList());

        CommonResponse resp = service.checkEligibility("MGEN");
        assertEquals("200", resp.getStatusCode());
        verify(sessionRepository, times(1)).save(sessionCaptor.capture());
        FaceScanSessionModel saved = sessionCaptor.getValue();
        assertNotNull(saved.getFedId());
        assertTrue(saved.getFedId().startsWith("FED"));
        assertTrue(saved.getFedId().length() > 3);
    }
    
    @Test
    void checkEligibility_memberCodeMismatch_returns403() {
        when(securityContext.authenticatedMemberCode()).thenReturn("DIFFERENT_MEMBER");
        // Request memberCode doesn't match authenticated memberCode
        CommonResponse resp = service.checkEligibility("M_REQUESTED");
        assertEquals("403", resp.getStatusCode());
        assertEquals(403, ((ErrorResponse)resp.getData()).statusCode());
    }
    
    @Test
    void acceptTnc_memberCodeMismatch_returns403() {
        when(securityContext.authenticatedMemberCode()).thenReturn("DIFFERENT_MEMBER");
        
        FaceScanTncRequest req = new FaceScanTncRequest();
        req.setFedId("F_MISMATCH");
        req.setConsent(true);
        
        FaceScanSessionModel existing = new FaceScanSessionModel("F_MISMATCH", "M_SESSION");
        existing.setId(1L);
        when(sessionRepository.findByFedId("F_MISMATCH")).thenReturn(Optional.of(existing));
        
        CommonResponse resp = service.acceptTncAndInitiateSession(req);
        assertEquals("403", resp.getStatusCode());
        assertEquals(403, ((ErrorResponse)resp.getData()).statusCode());
    }
    
    @Test
    void storeFaceScanResult_memberCodeMismatch_returns403() {
        when(securityContext.authenticatedMemberCode()).thenReturn("DIFFERENT_MEMBER");
        
        FaceScanResultRequest req = new FaceScanResultRequest();
        req.setSessionId("S_MISMATCH");
        req.setEndTime("2025-11-03T10:00:00");
        req.setScanData(Map.of("k","v"));
        
        FaceScanSessionModel session = new FaceScanSessionModel("F_SESSION", "M_SESSION");
        when(sessionRepository.findBySessionId("S_MISMATCH")).thenReturn(Optional.of(session));
        
        CommonResponse resp = service.storeFaceScanResult(req);
        assertEquals("403", resp.getStatusCode());
        assertEquals(403, ((ErrorResponse)resp.getData()).statusCode());
    }
    
    @Test
    void getFaceScanHistory_memberCodeMismatch_returns403() {
        when(securityContext.authenticatedMemberCode()).thenReturn("DIFFERENT_MEMBER");
        // Request memberCode doesn't match authenticated memberCode
        FaceScanHistoryRequest req = new FaceScanHistoryRequest();
        req.setMemberCode("M_REQUESTED");
        
        CommonResponse resp = service.getFaceScanHistory(req);
        assertEquals("403", resp.getStatusCode());
        assertEquals(403, ((ErrorResponse)resp.getData()).statusCode());
    }
    
    @Test
    void getFaceScanHistory_emptyResults_returns404() {
        when(securityContext.authenticatedMemberCode()).thenReturn("M_EMPTY");
        MembershipModel m = new MembershipModel();
        m.setMemberCode("M_EMPTY");
        m.setAccountCode("A_EMPTY");
        m.setMemType("C");
        when(membershipRepository.findByMemberCode("M_EMPTY")).thenReturn(Optional.of(m));
        
        FaceScanHistoryRequest req = new FaceScanHistoryRequest();
        req.setMemberCode("M_EMPTY");
        when(faceScanResultRepository.findByMemberCodeAndScanResultOrderByEndTimeDesc("M_EMPTY","SUCCESS")).thenReturn(Collections.emptyList());

        CommonResponse resp = service.getFaceScanHistory(req);
        assertEquals("404", resp.getStatusCode());
        assertEquals(404, ((ErrorResponse)resp.getData()).statusCode());
    }
    
    @Test
    void fetchFaceScanResult_notFound_returns404() {
        when(faceScanResultRepository.findBySessionId("SNOTFOUND")).thenReturn(Optional.empty());
        CommonResponse resp = service.fetchFaceScanResult("SNOTFOUND");
        assertEquals("404", resp.getStatusCode());
        assertEquals(404, ((ErrorResponse)resp.getData()).statusCode());
    }
}
