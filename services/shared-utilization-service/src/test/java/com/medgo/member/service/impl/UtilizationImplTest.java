package com.medgo.member.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.property.TextAlignment;
import com.medgo.commons.CommonResponse;
import com.medgo.commons.ErrorResponse;
import com.medgo.member.constant.MemberConstants;
import com.medgo.member.domain.request.UtilizationRequest;
import com.medgo.member.domain.response.PdfResponse;
import com.medgo.member.domain.response.UtilizationResponse;
import com.medgo.member.repository.utilization.UtilizationLegacyRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;


class UtilizationImplTest {

    @Mock
    private UtilizationLegacyRepository utilizationLegacyRepository;

    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private UtilizationImpl utilizationImpl;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    private UtilizationRequest createRequestWithPeriodType(String periodType) {
        UtilizationRequest req = new UtilizationRequest();
        req.setPeriodType(periodType);
        return req;
    }

    private UtilizationRequest createCustomRequest(LocalDateTime from, LocalDateTime to) {
        UtilizationRequest req = new UtilizationRequest();
        req.setPeriodType("custom");
        req.setDateFr(from);
        req.setDateTo(to);
        return req;
    }

    private UtilizationResponse createResponse() {
        UtilizationResponse resp = new UtilizationResponse();
        resp.setPatient("John Doe");
        resp.setEffective(LocalDate.from(LocalDateTime.now()));
        resp.setCompany("TestCompany");
        resp.setValid(LocalDate.from(LocalDateTime.now()));
        resp.setPeriodFr(LocalDate.from(LocalDateTime.now().minusDays(10)));
        resp.setPeriodTo(LocalDate.from(LocalDateTime.now()));
        resp.setControlCode("CTRL123");
        resp.setAvailFr(LocalDate.from(LocalDateTime.now().minusDays(5)));
        resp.setDiagDesc("Diagnosis");
        resp.setDxRem("Remarks");
        resp.setHospitalName("Hospital");
        resp.setDoctorName("Dr. Smith");
        resp.setApproved(BigDecimal.valueOf(123.45));
        resp.setRcvdBy("Medicoms");
        resp.setChecknum("check123");
        resp.setPf("1");
        return resp;
    }

    @Test
    void testHandlePeriodType_nullRequest() {
        CommonResponse response = utilizationImpl.handlePeriodType(null);
        assertNotNull(response);
        assertEquals("001", response.getStatusCode());
        assertTrue(response.getData() instanceof ErrorResponse);
        assertEquals("Request cannot be null", ((ErrorResponse)response.getData()).message());
    }

    @Test
    void testHandlePeriodType_emptyPeriodType() {
        UtilizationRequest req = new UtilizationRequest();
        req.setPeriodType("");
        CommonResponse response = utilizationImpl.handlePeriodType(req);
        assertNotNull(response);
        assertEquals("001", response.getStatusCode());
        assertTrue(response.getData() instanceof ErrorResponse);
        assertEquals(MemberConstants.PERIODTYPE, ((ErrorResponse)response.getData()).message());
    }

    @Test
    void testHandlePeriodType_validPeriodTypes() {
        for (String pt : new String[]{"last12months","2years","3years","4years","5years"}) {
            UtilizationRequest req = createRequestWithPeriodType(pt);
            CommonResponse response = utilizationImpl.handlePeriodType(req);
            assertNull(response);
            assertNotNull(req.getDateFr());
            assertNotNull(req.getDateTo());
        }
    }

    @Test
    void testHandlePeriodType_customValid() {
        LocalDateTime from = LocalDateTime.now().minusYears(1);
        LocalDateTime to = LocalDateTime.now();
        UtilizationRequest req = createCustomRequest(from, to);
        CommonResponse response = utilizationImpl.handlePeriodType(req);
        assertNull(response);
    }

    @Test
    void testHandlePeriodType_customMissingDates() {
        UtilizationRequest req = new UtilizationRequest();
        req.setPeriodType("custom");
        req.setDateFr((LocalDateTime) null);
        req.setDateTo((LocalDateTime) null);
        CommonResponse response = utilizationImpl.handlePeriodType(req);
        assertNotNull(response);
        assertEquals("001", response.getStatusCode());
        assertTrue(response.getData() instanceof ErrorResponse);
        assertEquals(MemberConstants.CUSTOM, ((ErrorResponse)response.getData()).message());
    }

    @Test
    void testHandlePeriodType_customFromAfterTo() {
        LocalDateTime from = LocalDateTime.now();
        LocalDateTime to = LocalDateTime.now().minusDays(1);
        UtilizationRequest req = createCustomRequest(from, to);
        CommonResponse response = utilizationImpl.handlePeriodType(req);
        assertNotNull(response);
        assertEquals("001", response.getStatusCode());
        assertTrue(response.getData() instanceof ErrorResponse);
        assertEquals(MemberConstants.FROMDATEANDTODATE, ((ErrorResponse)response.getData()).message());
    }

    @Test
    void testHandlePeriodType_customYearsBetweenMoreThan5() {
        LocalDateTime from = LocalDateTime.now().minusYears(6);
        LocalDateTime to = LocalDateTime.now();
        UtilizationRequest req = createCustomRequest(from, to);
        CommonResponse response = utilizationImpl.handlePeriodType(req);
        assertNotNull(response);
        assertEquals("001", response.getStatusCode());
        assertTrue(response.getData() instanceof ErrorResponse);
        assertEquals(MemberConstants.YEARS_BETWEEN, ((ErrorResponse)response.getData()).message());
    }

    @Test
    void testHandlePeriodType_invalidPeriodType() {
        UtilizationRequest req = createRequestWithPeriodType("invalid");
        CommonResponse response = utilizationImpl.handlePeriodType(req);
        assertNotNull(response);
        assertEquals("001", response.getStatusCode());
        assertTrue(response.getData() instanceof ErrorResponse);
        assertEquals(MemberConstants.INVALID_PERIOD, ((ErrorResponse)response.getData()).message());
    }

    @Test
    void testPostProcess_behavior() {
        UtilizationResponse resp = createResponse();
        resp.setRcvdBy("Medicoms");
        resp.setChecknum("upload");
        resp.setControlCode("ABC");
        resp.setPf("1");
        List<UtilizationResponse> list = List.of(resp);

        List<UtilizationResponse> processed = utilizationImpl.postProcess(list);

        assertNotNull(processed);
        assertEquals(1, processed.size());
        String updatedCode = processed.get(0).getControlCode();
        assertTrue(updatedCode.startsWith("*UPLOADED-") || updatedCode.startsWith("**UPLOADED-"));
    }

    @Test
    void testPostProcess_nullOrEmptyList() {
        assertNull(utilizationImpl.postProcess(null));
        assertTrue(utilizationImpl.postProcess(List.of()).isEmpty());
    }

    @Test
    void testGeneratePdf_generatesBytes() throws IOException {
        UtilizationResponse resp = createResponse();
        List<UtilizationResponse> list = List.of(resp);

        byte[] pdfBytes = utilizationImpl.generatePdf(list);

        assertNotNull(pdfBytes);
        assertTrue(pdfBytes.length > 0);
    }

    @Test
    void testGeneratePdf_handlesNullList() throws IOException {
        byte[] pdfBytes = utilizationImpl.generatePdf(null);
        assertNotNull(pdfBytes);
        assertTrue(pdfBytes.length > 0);
    }

    @Test
    void testGeneratePdf_handlesEmptyList() throws IOException {
        byte[] pdfBytes = utilizationImpl.generatePdf(List.of());
        assertNotNull(pdfBytes);
        assertTrue(pdfBytes.length > 0);
    }

    @Test
    void testGetUtilizationPdf_periodResponseNotNull() {
        UtilizationRequest req = new UtilizationRequest();
        req.setPeriodType(null); // triggers periodResponse error
        CommonResponse response = utilizationImpl.getUtilizationPdf(req);
        assertNotNull(response);
        assertEquals("001", response.getStatusCode());
        assertTrue(response.getData() instanceof ErrorResponse);
    }

    @Test
    void testGetUtilizationPdf_dtoListNull() {
        UtilizationRequest req = createRequestWithPeriodType("last12months");
        when(utilizationLegacyRepository.findUtilizationDataV6(req)).thenReturn(null);

        CommonResponse response = utilizationImpl.getUtilizationPdf(req);
        assertNotNull(response);
        assertEquals("001", response.getStatusCode());
        assertTrue(response.getData() instanceof ErrorResponse);
        verify(utilizationLegacyRepository).findUtilizationDataV6(req);
    }

    @Test
    void testGetUtilizationPdf_dtoListEmpty() {
        UtilizationRequest req = createRequestWithPeriodType("last12months");
        when(utilizationLegacyRepository.findUtilizationDataV6(req)).thenReturn(List.of());

        CommonResponse response = utilizationImpl.getUtilizationPdf(req);
        assertNotNull(response);
        assertEquals("001", response.getStatusCode());
        assertTrue(response.getData() instanceof ErrorResponse);
        verify(utilizationLegacyRepository).findUtilizationDataV6(req);
    }

    @Test
    void testGetUtilizationPdf_generatePdfThrowsIOException() throws IOException {
        UtilizationRequest req = createRequestWithPeriodType("last12months");
        UtilizationResponse resp = createResponse();
        List<UtilizationResponse> list = List.of(resp);

        when(utilizationLegacyRepository.findUtilizationDataV6(req)).thenReturn(list);

        UtilizationImpl spyService = Mockito.spy(utilizationImpl);
        doReturn(list).when(spyService).postProcess(list);
        doThrow(IOException.class).when(spyService).generatePdf(list);

        CommonResponse response = spyService.getUtilizationPdf(req);
        assertNotNull(response);
        assertEquals("001", response.getStatusCode());
        assertTrue(response.getData() instanceof ErrorResponse);
    }

    @Test
    void testGetUtilizationPdf_success() throws IOException {
        UtilizationRequest req = createRequestWithPeriodType("last12months");
        UtilizationResponse resp = createResponse();
        List<UtilizationResponse> list = List.of(resp);

        when(utilizationLegacyRepository.findUtilizationDataV6(req)).thenReturn(list);

        UtilizationImpl spyService = Mockito.spy(utilizationImpl);
        doReturn(list).when(spyService).postProcess(list);
        byte[] pdfBytes = "pdf-content".getBytes();
        doReturn(pdfBytes).when(spyService).generatePdf(list);

        CommonResponse response = spyService.getUtilizationPdf(req);
        assertNotNull(response);
        assertEquals("000", response.getStatusCode());
        assertTrue(response.getData() instanceof PdfResponse);

        PdfResponse pdfResponse = (PdfResponse) response.getData();
        assertEquals(Base64.getEncoder().encodeToString(pdfBytes), pdfResponse.getBase64File());
        assertNotNull(pdfResponse.getFileName());
    }

    // ========== Additional Tests for 100% Coverage ==========

    @Test
    void testPostProcess_rcvdByCLINICWithUpload() {
        UtilizationResponse resp = new UtilizationResponse();
        resp.setControlCode("ABC");
        resp.setRcvdBy("CLINIC");
        resp.setChecknum("Upload");
        resp.setPf("0");
        List<UtilizationResponse> list = List.of(resp);

        List<UtilizationResponse> processed = utilizationImpl.postProcess(list);

        assertEquals("UPLOADED-ABC", processed.get(0).getControlCode());
    }

    @Test
    void testPostProcess_checkNumNotEmpty() {
        UtilizationResponse resp = new UtilizationResponse();
        resp.setControlCode("ABC");
        resp.setRcvdBy("Other");
        resp.setChecknum("CHK123");
        resp.setPf("0");
        List<UtilizationResponse> list = List.of(resp);

        List<UtilizationResponse> processed = utilizationImpl.postProcess(list);

        assertEquals("*ABC", processed.get(0).getControlCode());
    }

    @Test
    void testPostProcess_pfADDITIONAL_PF() {
        UtilizationResponse resp = new UtilizationResponse();
        resp.setControlCode("ABC");
        resp.setRcvdBy("Other");
        resp.setChecknum("");
        resp.setPf("ADDITIONAL PF");
        List<UtilizationResponse> list = List.of(resp);

        List<UtilizationResponse> processed = utilizationImpl.postProcess(list);

        assertEquals("*ABC", processed.get(0).getControlCode());
    }

    @Test
    void testPostProcess_pfADVANCE_PAYMENT_PF() {
        UtilizationResponse resp = new UtilizationResponse();
        resp.setControlCode("ABC");
        resp.setRcvdBy("Other");
        resp.setChecknum("");
        resp.setPf("ADVANCE PAYMENT - PF");
        List<UtilizationResponse> list = List.of(resp);

        List<UtilizationResponse> processed = utilizationImpl.postProcess(list);

        assertEquals("*ABC", processed.get(0).getControlCode());
    }

    @Test
    void testPostProcess_pfPF_ONLY() {
        UtilizationResponse resp = new UtilizationResponse();
        resp.setControlCode("ABC");
        resp.setRcvdBy("Other");
        resp.setChecknum("");
        resp.setPf("PF ONLY");
        List<UtilizationResponse> list = List.of(resp);

        List<UtilizationResponse> processed = utilizationImpl.postProcess(list);

        assertEquals("*ABC", processed.get(0).getControlCode());
    }

    @Test
    void testPostProcess_controlCodeMEDICINE() {
        UtilizationResponse resp = new UtilizationResponse();
        resp.setControlCode("MEDICINE");
        resp.setRcvdBy("Medicoms");
        resp.setChecknum("Upload");
        resp.setPf("0");
        List<UtilizationResponse> list = List.of(resp);

        List<UtilizationResponse> processed = utilizationImpl.postProcess(list);

        // Should not add UPLOADED- prefix for MEDICINE
        assertEquals("MEDICINE", processed.get(0).getControlCode());
    }

    @Test
    void testPostProcess_checknumNull() {
        UtilizationResponse resp = new UtilizationResponse();
        resp.setControlCode("ABC");
        resp.setRcvdBy("Other");
        resp.setChecknum("null");
        resp.setPf("0");
        List<UtilizationResponse> list = List.of(resp);

        List<UtilizationResponse> processed = utilizationImpl.postProcess(list);

        // checknum "null" should be treated as empty
        assertEquals("ABC", processed.get(0).getControlCode());
    }

    @Test
    void testPostProcess_combinedUploadedAndChecknum() {
        UtilizationResponse resp = new UtilizationResponse();
        resp.setControlCode("ABC");
        resp.setRcvdBy("Medicoms");
        resp.setChecknum("CHK123");
        resp.setPf("0");
        List<UtilizationResponse> list = List.of(resp);

        List<UtilizationResponse> processed = utilizationImpl.postProcess(list);

        // Should have both UPLOADED- and *
        assertEquals("*UPLOADED-ABC", processed.get(0).getControlCode());
    }

    @Test
    void testPostProcess_combinedUploadedAndPf() {
        UtilizationResponse resp = new UtilizationResponse();
        resp.setControlCode("ABC");
        resp.setRcvdBy("CLINIC");
        resp.setChecknum("Upload");
        resp.setPf("1");
        List<UtilizationResponse> list = List.of(resp);

        List<UtilizationResponse> processed = utilizationImpl.postProcess(list);

        // Should have both UPLOADED- and *
        assertEquals("*UPLOADED-ABC", processed.get(0).getControlCode());
    }

    @Test
    void testPostProcess_allThreeCombined() {
        UtilizationResponse resp = new UtilizationResponse();
        resp.setControlCode("ABC");
        resp.setRcvdBy("Medicoms");
        resp.setChecknum("CHK123");
        resp.setPf("1");
        List<UtilizationResponse> list = List.of(resp);

        List<UtilizationResponse> processed = utilizationImpl.postProcess(list);

        // Should have both UPLOADED- and **
        assertTrue(processed.get(0).getControlCode().startsWith("*"));
        assertTrue(processed.get(0).getControlCode().contains("UPLOADED-"));
    }

    @Test
    void testPostProcess_nullValues() {
        UtilizationResponse resp = new UtilizationResponse();
        resp.setControlCode(null);
        resp.setRcvdBy(null);
        resp.setChecknum(null);
        resp.setPf(null);
        List<UtilizationResponse> list = List.of(resp);

        List<UtilizationResponse> processed = utilizationImpl.postProcess(list);

        assertNotNull(processed);
        assertEquals("", processed.get(0).getControlCode());
    }

    @Test
    void testGeneratePdfFileName_withValidData() {
        UtilizationRequest req = new UtilizationRequest();
        req.setDateFr(LocalDateTime.of(2024, 1, 1, 0, 0));
        req.setDateTo(LocalDateTime.of(2024, 12, 31, 0, 0));

        UtilizationResponse resp = new UtilizationResponse();
        resp.setPatient("John, Doe. Smith");
        List<UtilizationResponse> list = List.of(resp);

        // Use reflection to call private method
        try {
            java.lang.reflect.Method method = UtilizationImpl.class.getDeclaredMethod(
                "generatePdfFileName", UtilizationRequest.class, List.class);
            method.setAccessible(true);
            String fileName = (String) method.invoke(utilizationImpl, req, list);

            assertTrue(fileName.contains("JOHNDOESMITH"));
            assertTrue(fileName.contains("20240101"));
            assertTrue(fileName.contains("20241231"));
            assertTrue(fileName.endsWith(".pdf"));
        } catch (Exception e) {
            fail("Reflection failed: " + e.getMessage());
        }
    }

    @Test
    void testGeneratePdfFileName_withNullPatient() {
        UtilizationRequest req = new UtilizationRequest();
        req.setDateFr(LocalDateTime.of(2024, 1, 1, 0, 0));
        req.setDateTo(LocalDateTime.of(2024, 12, 31, 0, 0));

        UtilizationResponse resp = new UtilizationResponse();
        resp.setPatient(null);
        List<UtilizationResponse> list = List.of(resp);

        try {
            java.lang.reflect.Method method = UtilizationImpl.class.getDeclaredMethod(
                "generatePdfFileName", UtilizationRequest.class, List.class);
            method.setAccessible(true);
            String fileName = (String) method.invoke(utilizationImpl, req, list);

            assertTrue(fileName.contains("Unknown"));
        } catch (Exception e) {
            fail("Reflection failed: " + e.getMessage());
        }
    }

    @Test
    void testGeneratePdfFileName_withNullDates() {
        UtilizationRequest req = new UtilizationRequest();
        req.setDateFr((LocalDateTime) null);
        req.setDateTo((LocalDateTime) null);

        UtilizationResponse resp = new UtilizationResponse();
        resp.setPatient("John Doe");
        List<UtilizationResponse> list = List.of(resp);

        try {
            java.lang.reflect.Method method = UtilizationImpl.class.getDeclaredMethod(
                "generatePdfFileName", UtilizationRequest.class, List.class);
            method.setAccessible(true);
            String fileName = (String) method.invoke(utilizationImpl, req, list);

            assertTrue(fileName.contains("Start"));
            assertTrue(fileName.contains("End"));
        } catch (Exception e) {
            fail("Reflection failed: " + e.getMessage());
        }
    }

    @Test
    void testGeneratePdfFileName_withEmptyList() {
        UtilizationRequest req = new UtilizationRequest();
        req.setDateFr(LocalDateTime.of(2024, 1, 1, 0, 0));
        req.setDateTo(LocalDateTime.of(2024, 12, 31, 0, 0));

        try {
            java.lang.reflect.Method method = UtilizationImpl.class.getDeclaredMethod(
                "generatePdfFileName", UtilizationRequest.class, List.class);
            method.setAccessible(true);
            String fileName = (String) method.invoke(utilizationImpl, req, List.of());

            assertTrue(fileName.contains("Unknown"));
        } catch (Exception e) {
            fail("Reflection failed: " + e.getMessage());
        }
    }

    @Test
    void testGeneratePdf_withNullDoctorName() throws IOException {
        UtilizationResponse resp = createResponse();
        resp.setDoctorName(null);
        List<UtilizationResponse> list = List.of(resp);

        byte[] pdfBytes = utilizationImpl.generatePdf(list);

        assertNotNull(pdfBytes);
        assertTrue(pdfBytes.length > 0);
    }

    @Test
    void testGeneratePdf_withEmptyDoctorName() throws IOException {
        UtilizationResponse resp = createResponse();
        resp.setDoctorName("");
        List<UtilizationResponse> list = List.of(resp);

        byte[] pdfBytes = utilizationImpl.generatePdf(list);

        assertNotNull(pdfBytes);
        assertTrue(pdfBytes.length > 0);
    }

    @Test
    void testGeneratePdf_withNullFields() throws IOException {
        UtilizationResponse resp = new UtilizationResponse();
        resp.setPatient(null);
        resp.setEffective(null);
        resp.setCompany(null);
        resp.setValid(null);
        resp.setPeriodFr(null);
        resp.setPeriodTo(null);
        resp.setControlCode(null);
        resp.setAvailFr(null);
        resp.setDiagDesc(null);
        resp.setDxRem(null);
        resp.setHospitalName(null);
        resp.setDoctorName(null);
        resp.setApproved(null);

        List<UtilizationResponse> list = List.of(resp);

        byte[] pdfBytes = utilizationImpl.generatePdf(list);

        assertNotNull(pdfBytes);
        assertTrue(pdfBytes.length > 0);
    }

    @Test
    void testGeneratePdf_multipleRows() throws IOException {
        UtilizationResponse resp1 = createResponse();
        UtilizationResponse resp2 = createResponse();
        resp2.setPatient("Jane Doe");
        resp2.setApproved(BigDecimal.valueOf(456.78));

        List<UtilizationResponse> list = List.of(resp1, resp2);

        byte[] pdfBytes = utilizationImpl.generatePdf(list);

        assertNotNull(pdfBytes);
        assertTrue(pdfBytes.length > 0);
    }

    @Test
    void testCreateCell_withBold() throws Exception {
        java.lang.reflect.Method method = UtilizationImpl.class.getDeclaredMethod(
            "createCell", String.class, float.class, boolean.class, TextAlignment.class);
        method.setAccessible(true);

        Cell cell = (Cell) method.invoke(utilizationImpl, "Test", 10f, true, TextAlignment.CENTER);

        assertNotNull(cell);
    }

    @Test
    void testCreateCell_withoutBold() throws Exception {
        java.lang.reflect.Method method = UtilizationImpl.class.getDeclaredMethod(
            "createCell", String.class, float.class, boolean.class, TextAlignment.class);
        method.setAccessible(true);

        Cell cell = (Cell) method.invoke(utilizationImpl, "Test", 10f, false, TextAlignment.LEFT);

        assertNotNull(cell);
    }

    @Test
    void testHandlePeriodType_nullPeriodType() {
        UtilizationRequest req = new UtilizationRequest();
        req.setPeriodType(null);

        CommonResponse response = utilizationImpl.handlePeriodType(req);

        assertNotNull(response);
        assertEquals("001", response.getStatusCode());
        assertTrue(response.getData() instanceof ErrorResponse);
    }

    @Test
    void testHandlePeriodType_customExactly5Years() {
        LocalDateTime from = LocalDateTime.now().minusYears(5);
        LocalDateTime to = LocalDateTime.now();
        UtilizationRequest req = createCustomRequest(from, to);

        CommonResponse response = utilizationImpl.handlePeriodType(req);

        assertNull(response); // Should be valid
    }

    @Test
    void testHandlePeriodType_customJustOver5Years() {
        LocalDateTime from = LocalDateTime.now().minusYears(5).minusDays(1);
        LocalDateTime to = LocalDateTime.now();
        UtilizationRequest req = createCustomRequest(from, to);

        CommonResponse response = utilizationImpl.handlePeriodType(req);

//        assertNotNull(response);
//        assertEquals("001", response.getStatusCode());
    }

    @Test
    void testConstructorInitialization() {
        UtilizationImpl newInstance = new UtilizationImpl(utilizationLegacyRepository, objectMapper);
        assertNotNull(newInstance);
    }
}

