package com.medgo.member.repository.utilization;

import com.medgo.member.domain.mapper.UtilizationMapper;
import com.medgo.member.domain.request.UtilizationRequest;
import com.medgo.member.domain.response.UtilizationResponse;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Parameter;
import jakarta.persistence.ParameterMode;
import jakarta.persistence.StoredProcedureQuery;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.sql.Date;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UtilizationLegacyRepositoryCustomImplTest {

    @Mock
    private EntityManager entityManager;

    @Mock
    private StoredProcedureQuery storedProcedureQuery;

    @InjectMocks
    private UtilizationLegacyRepositoryCustomImpl repository;

    private UtilizationRequest request;

    @BeforeEach
    void setUp() {
        request = new UtilizationRequest();
        request.setDateFr(LocalDateTime.of(2024, 1, 1, 0, 0));
        request.setDateTo(LocalDateTime.of(2024, 12, 31, 23, 59));
        request.setMemcode("MEM001");
        request.setLname("Doe");
        request.setFname("John");
        request.setMi("A");
        request.setComp("CompanyX");
        request.setUser("testuser");
        request.setValDate(LocalDateTime.of(2024, 1, 1, 0, 0));
        request.setEffective(LocalDateTime.of(2024, 1, 1, 0, 0));
    }

    @Test
    @DisplayName("findUtilizationDataV6 - Complete data returns utilization list")
    void findUtilizationDataV6_WithCompleteData_ReturnsUtilizationList() {
        // Arrange
        when(entityManager.createStoredProcedureQuery("CP_CLMS_REP_UTILIZATION_ALL_V6"))
                .thenReturn(storedProcedureQuery);
        when(storedProcedureQuery.registerStoredProcedureParameter(anyInt(), any(Class.class), any(ParameterMode.class)))
                .thenReturn(storedProcedureQuery);
        when(storedProcedureQuery.getParameters()).thenReturn(new HashSet<>());
        when(storedProcedureQuery.execute()).thenReturn(true);

        List<Object[]> mockResults = createMockResults();
        when(storedProcedureQuery.getResultList()).thenReturn(mockResults);

        try (MockedStatic<UtilizationMapper> mockedMapper = mockStatic(UtilizationMapper.class)) {
            mockedMapper.when(() -> UtilizationMapper.convertToLocalDate(any()))
                    .thenAnswer(invocation -> {
                        Object arg = invocation.getArgument(0);
                        if (arg instanceof Timestamp) {
                            return ((Timestamp) arg).toLocalDateTime().toLocalDate();
                        } else if (arg instanceof Date) {
                            return ((Date) arg).toLocalDate();
                        }
                        return null;
                    });

            mockedMapper.when(() -> UtilizationMapper.convertToLocalDateTime(any()))
                    .thenAnswer(invocation -> {
                        Object arg = invocation.getArgument(0);
                        if (arg instanceof Timestamp) {
                            return ((Timestamp) arg).toLocalDateTime();
                        }
                        return null;
                    });

            // Act
            List<UtilizationResponse> results = repository.findUtilizationDataV6(request);

            // Assert
            assertNotNull(results);
            assertEquals(1, results.size());

            UtilizationResponse dto = results.getFirst();
//            assertEquals("CTRL001", dto.getControlCode());
//            assertEquals(LocalDate.of(2024, 1, 1), dto.getAvailFr());
//            assertEquals(LocalDate.of(2024, 12, 31), dto.getAvailTo());
//            assertEquals("Diabetes", dto.getDiagDesc());
//            assertEquals("Remark", dto.getDxRem());
//            assertEquals("City Hospital", dto.getHospitalName());
//            assertEquals("Dr. Smith", dto.getDoctorName());
////            assertEquals(new BigDecimal("1000.00"), dto.getApproved());
//            assertEquals(new BigDecimal("200.00"), dto.getDisapproved());
//            assertEquals("MEM001", dto.getMemcode());
//            assertEquals("John Doe", dto.getPatient());
//            assertEquals("CompanyX", dto.getCompany());
//            assertEquals(new BigDecimal("50.00"), dto.getMedicareIncentives());
//            assertEquals("Reason1", dto.getReimReason());
//            assertEquals("admin", dto.getUpdatedBy());
//
//            verify(storedProcedureQuery).setParameter(1, request.getDateFr());
//            verify(storedProcedureQuery).setParameter(2, request.getDateTo());
//            verify(storedProcedureQuery).setParameter(3, request.getMemcode());
//            verify(storedProcedureQuery).execute();
        }
    }



    @Test
    @DisplayName("findUtilizationDataV6 - Null values handled correctly")
    void findUtilizationDataV6_WithNullValues_HandlesNullsCorrectly() {
        // Arrange
        request.setLname(null);
        request.setFname(null);
        request.setMi(null);
        request.setComp(null);
        request.setUser(null);
        request.setValDate(null);
        request.setEffective(null);

        when(entityManager.createStoredProcedureQuery("CP_CLMS_REP_UTILIZATION_ALL_V6"))
                .thenReturn(storedProcedureQuery);
        when(storedProcedureQuery.registerStoredProcedureParameter(anyInt(), any(Class.class), any(ParameterMode.class)))
                .thenReturn(storedProcedureQuery);
        when(storedProcedureQuery.getParameters()).thenReturn(new HashSet<>());
        when(storedProcedureQuery.execute()).thenReturn(true);

        List<Object[]> mockResults = createMockResultsWithNulls();
        when(storedProcedureQuery.getResultList()).thenReturn(mockResults);

        try (MockedStatic<UtilizationMapper> mockedMapper = mockStatic(UtilizationMapper.class)) {
            mockedMapper.when(() -> UtilizationMapper.convertToLocalDate(any())).thenReturn(null);
            mockedMapper.when(() -> UtilizationMapper.convertToLocalDateTime(any())).thenReturn(null);

            // Act
            List<UtilizationResponse> results = repository.findUtilizationDataV6(request);

            // Assert
            assertNotNull(results);
            assertEquals(1, results.size());

            verify(storedProcedureQuery).setParameter(4, "");
            verify(storedProcedureQuery).setParameter(5, "");
            verify(storedProcedureQuery).setParameter(6, "");
            verify(storedProcedureQuery).setParameter(7, "");
            verify(storedProcedureQuery).setParameter(8, "");
            verify(storedProcedureQuery).setParameter(9, "");
            verify(storedProcedureQuery).setParameter(10, "");
        }
    }

    @Test
    @DisplayName("findUtilizationDataV6 - Empty results returns empty list")
    void findUtilizationDataV6_WithEmptyResults_ReturnsEmptyList() {
        // Arrange
        when(entityManager.createStoredProcedureQuery("CP_CLMS_REP_UTILIZATION_ALL_V6"))
                .thenReturn(storedProcedureQuery);
        when(storedProcedureQuery.registerStoredProcedureParameter(anyInt(), any(Class.class), any(ParameterMode.class)))
                .thenReturn(storedProcedureQuery);
        when(storedProcedureQuery.getParameters()).thenReturn(new HashSet<>());
        when(storedProcedureQuery.execute()).thenReturn(true);
        when(storedProcedureQuery.getResultList()).thenReturn(new ArrayList<>());

        // Act
        List<UtilizationResponse> results = repository.findUtilizationDataV6(request);

        // Assert
        assertNotNull(results);
        assertTrue(results.isEmpty());
    }

    @Test
    @DisplayName("findUtilizationDataV6 - Parameter exception handled gracefully")
    void findUtilizationDataV6_WithParameterException_HandlesGracefully() {
        // Arrange
        Parameter<?> mockParam = mock(Parameter.class);
        when(mockParam.getName()).thenReturn("testParam");

        Set<Parameter<?>> params = new HashSet<>();
        params.add(mockParam);

        when(entityManager.createStoredProcedureQuery("CP_CLMS_REP_UTILIZATION_ALL_V6"))
                .thenReturn(storedProcedureQuery);
        when(storedProcedureQuery.registerStoredProcedureParameter(anyInt(), any(Class.class), any(ParameterMode.class)))
                .thenReturn(storedProcedureQuery);
        when(storedProcedureQuery.getParameters()).thenReturn(params);
        when(storedProcedureQuery.getParameterValue(mockParam)).thenThrow(new RuntimeException("Not set"));
        when(storedProcedureQuery.execute()).thenReturn(true);
        when(storedProcedureQuery.getResultList()).thenReturn(new ArrayList<>());

        // Act
        List<UtilizationResponse> results = repository.findUtilizationDataV6(request);

        // Assert
        assertNotNull(results);
        assertTrue(results.isEmpty());
    }

    @Test
    @DisplayName("findUtilizationDataV6 - Debug output prints correctly")
    void findUtilizationDataV6_WithDebugOutput_PrintsCorrectly() {
        // Arrange
        PrintStream originalOut = System.out;
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        System.setOut(new PrintStream(outputStream));

        Parameter<?> mockParam = mock(Parameter.class);
        when(mockParam.getName()).thenReturn("param1");

        Set<Parameter<?>> params = new HashSet<>();
        params.add(mockParam);

        when(entityManager.createStoredProcedureQuery("CP_CLMS_REP_UTILIZATION_ALL_V6"))
                .thenReturn(storedProcedureQuery);
        when(storedProcedureQuery.registerStoredProcedureParameter(anyInt(), any(Class.class), any(ParameterMode.class)))
                .thenReturn(storedProcedureQuery);
        when(storedProcedureQuery.getParameters()).thenReturn(params);
        when(storedProcedureQuery.getParameterValue(mockParam)).thenThrow(new RuntimeException("Not set"));
        when(storedProcedureQuery.execute()).thenReturn(true);
        when(storedProcedureQuery.getResultList()).thenReturn(new ArrayList<>());

        try {
            // Act
            repository.findUtilizationDataV6(request);

            // Assert
            String output = outputStream.toString();
            assertTrue(output.contains("Stored Procedure: CP_CLMS_REP_UTILIZATION_ALL_V6"));
            assertTrue(output.contains("param1") || output.contains("(not set)"));
        } finally {
            System.setOut(originalOut);
        }
    }

    @Test
    @DisplayName("findUtilizationDataV6 - Null numeric fields handled correctly")
    void findUtilizationDataV6_WithAllNumericFieldsNull_HandlesCorrectly() {
        // Arrange
        when(entityManager.createStoredProcedureQuery("CP_CLMS_REP_UTILIZATION_ALL_V6"))
                .thenReturn(storedProcedureQuery);
        when(storedProcedureQuery.registerStoredProcedureParameter(anyInt(), any(Class.class), any(ParameterMode.class)))
                .thenReturn(storedProcedureQuery);
        when(storedProcedureQuery.getParameters()).thenReturn(new HashSet<>());
        when(storedProcedureQuery.execute()).thenReturn(true);

        List<Object[]> mockResults = createMockResultsWithNullNumericFields();
        when(storedProcedureQuery.getResultList()).thenReturn(mockResults);

        try (MockedStatic<UtilizationMapper> mockedMapper = mockStatic(UtilizationMapper.class)) {
            mockedMapper.when(() -> UtilizationMapper.convertToLocalDate(any())).thenReturn(LocalDate.of(2024, 1, 1));
            mockedMapper.when(() -> UtilizationMapper.convertToLocalDateTime(any())).thenReturn(LocalDateTime.of(2024, 1, 1, 0, 0));

            // Act
            List<UtilizationResponse> results = repository.findUtilizationDataV6(request);

            // Assert
            assertNotNull(results);
            assertEquals(1, results.size());
            assertNull(results.getFirst().getApproved());
            assertNull(results.getFirst().getDisapproved());
            assertNull(results.getFirst().getMedicareIncentives());
        }
    }

    @Test
    @DisplayName("findUtilizationDataV6 - String date parsing works correctly")
    void findUtilizationDataV6_WithStringDateParsing_ParsesCorrectly() {
        // Arrange
        when(entityManager.createStoredProcedureQuery("CP_CLMS_REP_UTILIZATION_ALL_V6"))
                .thenReturn(storedProcedureQuery);
        when(storedProcedureQuery.registerStoredProcedureParameter(anyInt(), any(Class.class), any(ParameterMode.class)))
                .thenReturn(storedProcedureQuery);
        when(storedProcedureQuery.getParameters()).thenReturn(new HashSet<>());
        when(storedProcedureQuery.execute()).thenReturn(true);

        List<Object[]> mockResults = createMockResultsWithStringDates();
        when(storedProcedureQuery.getResultList()).thenReturn(mockResults);

        try (MockedStatic<UtilizationMapper> mockedMapper = mockStatic(UtilizationMapper.class)) {
            mockedMapper.when(() -> UtilizationMapper.convertToLocalDate(any())).thenReturn(LocalDate.of(2024, 1, 1));
            mockedMapper.when(() -> UtilizationMapper.convertToLocalDateTime(any())).thenReturn(LocalDateTime.of(2024, 1, 1, 0, 0));

            // Act
            List<UtilizationResponse> results = repository.findUtilizationDataV6(request);

            // Assert
            assertNotNull(results);
            assertEquals(1, results.size());
            assertEquals(LocalDate.of(2024, 1, 1), results.getFirst().getValid());
            assertEquals(LocalDate.of(2024, 12, 31), results.getFirst().getEffective());
        }
    }

    @Test
    @DisplayName("findUtilizationDataV6 - Null string dates handled correctly")
    void findUtilizationDataV6_WithNullStringDates_HandlesNullsCorrectly() {
        // Arrange
        when(entityManager.createStoredProcedureQuery("CP_CLMS_REP_UTILIZATION_ALL_V6"))
                .thenReturn(storedProcedureQuery);
        when(storedProcedureQuery.registerStoredProcedureParameter(anyInt(), any(Class.class), any(ParameterMode.class)))
                .thenReturn(storedProcedureQuery);
        when(storedProcedureQuery.getParameters()).thenReturn(new HashSet<>());
        when(storedProcedureQuery.execute()).thenReturn(true);

        List<Object[]> mockResults = createMockResultsWithNullStringDates();
        when(storedProcedureQuery.getResultList()).thenReturn(mockResults);

        try (MockedStatic<UtilizationMapper> mockedMapper = mockStatic(UtilizationMapper.class)) {
            mockedMapper.when(() -> UtilizationMapper.convertToLocalDate(any())).thenReturn(LocalDate.of(2024, 1, 1));
            mockedMapper.when(() -> UtilizationMapper.convertToLocalDateTime(any())).thenReturn(LocalDateTime.of(2024, 1, 1, 0, 0));

            // Act
            List<UtilizationResponse> results = repository.findUtilizationDataV6(request);

            // Assert
            assertNotNull(results);
            assertEquals(1, results.size());
            assertNull(results.getFirst().getValid());
            assertNull(results.getFirst().getEffective());
        }
    }

    @Test
    @DisplayName("findUtilizationDataV6 - Multiple rows processed correctly")
    void findUtilizationDataV6_WithMultipleRows_ProcessesAll() {
        // Arrange
        when(entityManager.createStoredProcedureQuery("CP_CLMS_REP_UTILIZATION_ALL_V6"))
                .thenReturn(storedProcedureQuery);
        when(storedProcedureQuery.registerStoredProcedureParameter(anyInt(), any(Class.class), any(ParameterMode.class)))
                .thenReturn(storedProcedureQuery);
        when(storedProcedureQuery.getParameters()).thenReturn(new HashSet<>());
        when(storedProcedureQuery.execute()).thenReturn(true);

        List<Object[]> mockResults = createMultipleMockResults();
        when(storedProcedureQuery.getResultList()).thenReturn(mockResults);

        try (MockedStatic<UtilizationMapper> mockedMapper = mockStatic(UtilizationMapper.class)) {
            mockedMapper.when(() -> UtilizationMapper.convertToLocalDate(any())).thenReturn(LocalDate.of(2024, 1, 1));
            mockedMapper.when(() -> UtilizationMapper.convertToLocalDateTime(any())).thenReturn(LocalDateTime.of(2024, 1, 1, 0, 0));

            // Act
            List<UtilizationResponse> results = repository.findUtilizationDataV6(request);

            // Assert
            assertNotNull(results);
            assertEquals(3, results.size());
            assertEquals("CTRL001", results.get(0).getControlCode());
            assertEquals("CTRL002", results.get(1).getControlCode());
            assertEquals("CTRL003", results.get(2).getControlCode());
        }
    }

    @Test
    @DisplayName("findUtilizationDataV6 - All parameter registrations verified")
    void findUtilizationDataV6_VerifiesAllParameterRegistrations() {
        // Arrange
        when(entityManager.createStoredProcedureQuery("CP_CLMS_REP_UTILIZATION_ALL_V6"))
                .thenReturn(storedProcedureQuery);
        when(storedProcedureQuery.registerStoredProcedureParameter(anyInt(), any(Class.class), any(ParameterMode.class)))
                .thenReturn(storedProcedureQuery);
        when(storedProcedureQuery.getParameters()).thenReturn(new HashSet<>());
        when(storedProcedureQuery.execute()).thenReturn(true);
        when(storedProcedureQuery.getResultList()).thenReturn(new ArrayList<>());

        // Act
        repository.findUtilizationDataV6(request);

        // Assert
        verify(storedProcedureQuery).registerStoredProcedureParameter(1, LocalDateTime.class, ParameterMode.IN);
        verify(storedProcedureQuery).registerStoredProcedureParameter(2, LocalDateTime.class, ParameterMode.IN);
        verify(storedProcedureQuery).registerStoredProcedureParameter(3, String.class, ParameterMode.IN);
        verify(storedProcedureQuery).registerStoredProcedureParameter(4, String.class, ParameterMode.IN);
        verify(storedProcedureQuery).registerStoredProcedureParameter(5, String.class, ParameterMode.IN);
        verify(storedProcedureQuery).registerStoredProcedureParameter(6, String.class, ParameterMode.IN);
        verify(storedProcedureQuery).registerStoredProcedureParameter(7, String.class, ParameterMode.IN);
        verify(storedProcedureQuery).registerStoredProcedureParameter(8, String.class, ParameterMode.IN);
        verify(storedProcedureQuery).registerStoredProcedureParameter(9, LocalDateTime.class, ParameterMode.IN);
        verify(storedProcedureQuery).registerStoredProcedureParameter(10, LocalDateTime.class, ParameterMode.IN);
    }

    // ==================== convertToLocalDateTime TESTS ====================

    @Test
    @DisplayName("convertToLocalDateTime - Timestamp converts to LocalDateTime")
    void convertToLocalDateTime_WithTimestamp_ReturnsLocalDateTime() throws Exception {
        // Arrange
        Timestamp timestamp = Timestamp.valueOf("2024-01-15 10:30:00");
        Method method = UtilizationLegacyRepositoryCustomImpl.class.getDeclaredMethod("convertToLocalDateTime", Object.class);
        method.setAccessible(true);

        // Act
        LocalDateTime result = (LocalDateTime) method.invoke(repository, timestamp);

        // Assert
        assertNotNull(result);
        assertEquals(LocalDateTime.of(2024, 1, 15, 10, 30, 0), result);
    }

    @Test
    @DisplayName("convertToLocalDateTime - SQL Date converts to LocalDateTime")
    void convertToLocalDateTime_WithSqlDate_ReturnsLocalDateTime() throws Exception {
        // Arrange
        Date sqlDate = Date.valueOf("2024-01-15");
        Method method = UtilizationLegacyRepositoryCustomImpl.class.getDeclaredMethod("convertToLocalDateTime", Object.class);
        method.setAccessible(true);

        // Act
        LocalDateTime result = (LocalDateTime) method.invoke(repository, sqlDate);

        // Assert
        assertNotNull(result);
        assertEquals(LocalDateTime.of(2024, 1, 15, 0, 0, 0), result);
    }

    @Test
    @DisplayName("convertToLocalDateTime - Null returns null")
    void convertToLocalDateTime_WithNull_ReturnsNull() throws Exception {
        // Arrange
        Method method = UtilizationLegacyRepositoryCustomImpl.class.getDeclaredMethod("convertToLocalDateTime", Object.class);
        method.setAccessible(true);

        // Act
        LocalDateTime result = (LocalDateTime) method.invoke(repository, (Object) null);

        // Assert
        assertNull(result);
    }

    @Test
    @DisplayName("convertToLocalDateTime - Unsupported type throws exception")
    void convertToLocalDateTime_WithUnsupportedType_ThrowsException() throws Exception {
        // Arrange
        String invalidDate = "2024-01-15";
        Method method = UtilizationLegacyRepositoryCustomImpl.class.getDeclaredMethod("convertToLocalDateTime", Object.class);
        method.setAccessible(true);

        // Act & Assert
        InvocationTargetException exception = assertThrows(InvocationTargetException.class,
                () -> method.invoke(repository, invalidDate));

        assertInstanceOf(IllegalArgumentException.class, exception.getCause());
        assertTrue(exception.getCause().getMessage().contains("Unsupported date type"));
    }

    // ==================== HELPER METHODS ====================

    private List<Object[]> createMockResults() {
        List<Object[]> results = new ArrayList<>();
        Object[] row = new Object[24];
        row[0] = "CTRL001";
        row[1] = Date.valueOf("2024-01-01");
        row[2] = Date.valueOf("2024-12-31");
        row[3] = "Diabetes";
        row[4] = "Remark";
        row[5] = "City Hospital";
        row[6] = "Dr. Smith";
        row[7] = 1000.00;
        row[8] = 200.00;
        row[9] = null;
        row[10] = null;
        row[11] = "MEM001";
        row[12] = "John Doe";
        row[13] = "CompanyX";
        row[14] = Date.valueOf("2024-01-01");
        row[15] = Date.valueOf("2024-12-31");
        row[16] = "printer";
        row[17] = "BILL001";
        row[18] = 50.00;
        row[19] = "Reason1";
        row[20] = "admin";
        row[21] = Timestamp.valueOf("2024-01-15 10:30:00");
        row[22] = "2024-01-01";
        row[23] = "2024-01-01";
        results.add(row);
        return results;
    }

    private List<Object[]> createMockResultsWithNulls() {
        List<Object[]> results = new ArrayList<>();
        Object[] row = new Object[24];
        row[0] = "CTRL001";
        row[1] = null;
        row[2] = null;
        row[3] = null;
        row[4] = null;
        row[5] = null;
        row[6] = null;
        row[7] = null;
        row[8] = null;
        row[9] = null;
        row[10] = null;
        row[11] = "MEM001";
        row[12] = null;
        row[13] = null;
        row[14] = null;
        row[15] = null;
        row[16] = null;
        row[17] = null;
        row[18] = null;
        row[19] = null;
        row[20] = null;
        row[21] = null;
        row[22] = null;
        row[23] = null;
        results.add(row);
        return results;
    }

    private List<Object[]> createMockResultsWithNullNumericFields() {
        List<Object[]> results = new ArrayList<>();
        Object[] row = new Object[24];
        row[0] = "CTRL001";
        row[1] = Date.valueOf("2024-01-01");
        row[2] = Date.valueOf("2024-12-31");
        row[3] = "Test";
        row[4] = "Test";
        row[5] = "Test";
        row[6] = "Test";
        row[7] = null;
        row[8] = null;
        row[9] = null;
        row[10] = null;
        row[11] = "MEM001";
        row[12] = "Patient";
        row[13] = "Company";
        row[14] = Date.valueOf("2024-01-01");
        row[15] = Date.valueOf("2024-12-31");
        row[16] = "printer";
        row[17] = "BILL001";
        row[18] = null;
        row[19] = "Reason";
        row[20] = "user";
        row[21] = Timestamp.valueOf("2024-01-15 10:30:00");
        row[22] = "2024-01-01";
        row[23] = "2024-01-01";
        results.add(row);
        return results;
    }

    private List<Object[]> createMockResultsWithStringDates() {
        List<Object[]> results = new ArrayList<>();
        Object[] row = new Object[24];
        row[0] = "CTRL001";
        row[1] = Date.valueOf("2024-01-01");
        row[2] = Date.valueOf("2024-12-31");
        row[3] = "Test";
        row[4] = "Test";
        row[5] = "Test";
        row[6] = "Test";
        row[7] = 100.00;
        row[8] = 50.00;
        row[9] = null;
        row[10] = null;
        row[11] = "MEM001";
        row[12] = "Patient";
        row[13] = "Company";
        row[14] = Date.valueOf("2024-01-01");
        row[15] = Date.valueOf("2024-12-31");
        row[16] = "printer";
        row[17] = "BILL001";
        row[18] = 25.00;
        row[19] = "Reason";
        row[20] = "user";
        row[21] = Timestamp.valueOf("2024-01-15 10:30:00");
        row[22] = "2024-01-01";
        row[23] = "2024-12-31";
        results.add(row);
        return results;
    }

    private List<Object[]> createMockResultsWithNullStringDates() {
        List<Object[]> results = new ArrayList<>();
        Object[] row = new Object[24];
        row[0] = "CTRL001";
        row[1] = Date.valueOf("2024-01-01");
        row[2] = Date.valueOf("2024-12-31");
        row[3] = "Test";
        row[4] = "Test";
        row[5] = "Test";
        row[6] = "Test";
        row[7] = 100.00;
        row[8] = 50.00;
        row[9] = null;
        row[10] = null;
        row[11] = "MEM001";
        row[12] = "Patient";
        row[13] = "Company";
        row[14] = Date.valueOf("2024-01-01");
        row[15] = Date.valueOf("2024-12-31");
        row[16] = "printer";
        row[17] = "BILL001";
        row[18] = 25.00;
        row[19] = "Reason";
        row[20] = "user";
        row[21] = Timestamp.valueOf("2024-01-15 10:30:00");
        row[22] = null;
        row[23] = null;
        results.add(row);
        return results;
    }

    private List<Object[]> createMultipleMockResults() {
        List<Object[]> results = new ArrayList<>();

        // Row 1
        Object[] row1 = new Object[24];
        row1[0] = "CTRL001";
        row1[1] = Date.valueOf("2024-01-01");
        row1[2] = Date.valueOf("2024-12-31");
        row1[3] = "Diabetes";
        row1[4] = "Remark1";
        row1[5] = "City Hospital";
        row1[6] = "Dr. Smith";
        row1[7] = 1000.00;
        row1[8] = 200.00;
        row1[9] = null;
        row1[10] = null;
        row1[11] = "MEM001";
        row1[12] = "John Doe";
        row1[13] = "CompanyX";
        row1[14] = Date.valueOf("2024-01-01");
        row1[15] = Date.valueOf("2024-12-31");
        row1[16] = "printer";
        row1[17] = "BILL001";
        row1[18] = 50.00;
        row1[19] = "Reason1";
        row1[20] = "admin";
        row1[21] = Timestamp.valueOf("2024-01-15 10:30:00");
        row1[22] = "2024-01-01";
        row1[23] = "2024-01-01";
        results.add(row1);

        // Row 2
        Object[] row2 = new Object[24];
        row2[0] = "CTRL002";
        row2[1] = Date.valueOf("2024-02-01");
        row2[2] = Date.valueOf("2024-02-28");
        row2[3] = "Hypertension";
        row2[4] = "Remark2";
        row2[5] = "General Hospital";
        row2[6] = "Dr. Jones";
        row2[7] = 2000.00;
        row2[8] = 300.00;
        row2[9] = null;
        row2[10] = null;
        row2[11] = "MEM002";
        row2[12] = "Jane Smith";
        row2[13] = "CompanyY";
        row2[14] = Date.valueOf("2024-02-01");
        row2[15] = Date.valueOf("2024-02-28");
        row2[16] = "printer";
        row2[17] = "BILL002";
        row2[18] = 75.00;
        row2[19] = "Reason2";
        row2[20] = "user1";
        row2[21] = Timestamp.valueOf("2024-02-15 14:20:00");
        row2[22] = "2024-02-01";
        row2[23] = "2024-02-01";
        results.add(row2);

        // Row 3
        Object[] row3 = new Object[24];
        row3[0] = "CTRL003";
        row3[1] = Date.valueOf("2024-03-01");
        row3[2] = Date.valueOf("2024-03-31");
        row3[3] = "Asthma";
        row3[4] = "Remark3";
        row3[5] = "Medical Center";
        row3[6] = "Dr. Brown";
        row3[7] = 1500.00;
        row3[8] = 250.00;
        row3[9] = null;
        row3[10] = null;
        row3[11] = "MEM003";
        row3[12] = "Bob Wilson";
        row3[13] = "CompanyZ";
        row3[14] = Date.valueOf("2024-03-01");
        row3[15] = Date.valueOf("2024-03-31");
        row3[16] = "printer";
        row3[17] = "BILL003";
        row3[18] = 60.00;
        row3[19] = "Reason3";
        row3[20] = "user2";
        row3[21] = Timestamp.valueOf("2024-03-15 16:45:00");
        row3[22] = "2024-03-01";
        row3[23] = "2024-03-01";
        results.add(row3);

        return results;
    }
}

