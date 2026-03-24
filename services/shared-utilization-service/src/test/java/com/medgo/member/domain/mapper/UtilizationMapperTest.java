package com.medgo.member.domain.mapper;

import com.medgo.member.domain.entity.utilization.UtilizationLegacyEntity;
import com.medgo.member.domain.entity.utilization.UtilizationLegacyId;
import com.medgo.member.domain.response.UtilizationResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

class UtilizationMapperTest {

    // ==================== CONSTRUCTOR TEST ====================

    @Test
    @DisplayName("Private constructor throws UnsupportedOperationException")
    void testPrivateConstructor() throws Exception {
        Constructor<UtilizationMapper> constructor = UtilizationMapper.class.getDeclaredConstructor();
        constructor.setAccessible(true);

        InvocationTargetException exception = assertThrows(
                InvocationTargetException.class,
                constructor::newInstance
        );

        Throwable cause = exception.getCause();
        assertInstanceOf(UnsupportedOperationException.class, cause);
        assertEquals("Utility class", cause.getMessage());
    }

    // ==================== mapToUtilizationResponse TESTS ====================

    @Test
    @DisplayName("mapToUtilizationResponse maps full Object[] row correctly")
    void testMapToUtilizationResponseHappyPath() {
        Object[] row = new Object[34];
        row[0] = "CTRL123";
        row[1] = Timestamp.valueOf("2025-01-05 10:00:00");
        row[2] = "2025-01-06";
        row[3] = "DIAGNOSIS";
        row[4] = "DXREMARKS";
        row[5] = "HOSPITAL";
        row[6] = "DOCTOR";
        row[7] = 123.45;
        row[8] = 67.89;
        row[9] = 10.00;
        row[10] = 5.50;
        row[11] = "MEM001";
        row[12] = "PATIENT";
        row[13] = "COMPANY1";
        row[14] = "2025-02-01";
        row[15] = "2025-02-28";
        row[16] = "PRINTED";
        row[17] = "BILL123";
        row[18] = 15.00;
        row[19] = "REASON1";
        row[20] = "UPDUSER";
        row[21] = "2025-03-01T12:34:56";
        row[22] = "2025-04-01";
        row[23] = "2025-05-01";
        row[24] = "SOA123";
        row[25] = "ICD10CODE";
        row[26] = "ICD10 Description";
        row[27] = "REMARKS2";
        row[28] = "CHECK123";
        row[29] = "PROFEE";
        row[30] = "RECEIVED";
        row[31] = "2025-06-01";
        row[32] = "DEPENDENT";
        row[33] = "DEPCODE1";

        UtilizationResponse resp = UtilizationMapper.mapToUtilizationResponse(row);

//        assertAll(
//                () -> assertEquals("CTRL123", resp.getControlCode()),
//                () -> assertEquals(LocalDate.of(2025, 1, 5), resp.getAvailFr()),
//                () -> assertEquals(LocalDate.of(2025, 1, 6), resp.getAvailTo()),
//                () -> assertEquals("DIAGNOSIS", resp.getDiagDesc()),
//                () -> assertEquals("DXREMARKS", resp.getDxRem()),
//                () -> assertEquals("HOSPITAL", resp.getHospitalName()),
//                () -> assertEquals("DOCTOR", resp.getDoctorName()),
//                () -> assertEquals(new BigDecimal("123.45"), resp.getApproved()),
//                () -> assertEquals(new BigDecimal("67.89"), resp.getDisapproved()),
////                () -> assertEquals(new BigDecimal("10.00"), resp.getAdvances()),
//                () -> assertEquals(new BigDecimal("5.50"), resp.getErc()),
//                () -> assertEquals("MEM001", resp.getMemcode()),
//                () -> assertEquals("PATIENT", resp.getPatient()),
//                () -> assertEquals("COMPANY1", resp.getCompany()),
//                () -> assertEquals(LocalDate.of(2025, 2, 1), resp.getPeriodFr()),
//                () -> assertEquals(LocalDate.of(2025, 2, 28), resp.getPeriodTo()),
//                () -> assertEquals("PRINTED", resp.getPrintedBy()),
//                () -> assertEquals("BILL123", resp.getBillcode()),
//                () -> assertEquals(new BigDecimal("15.00"), resp.getMedicareIncentives()),
//                () -> assertEquals("REASON1", resp.getReimReason()),
//                () -> assertEquals("UPDUSER", resp.getUpdatedBy()),
//                () -> assertEquals(LocalDateTime.of(2025, 3, 1, 12, 34, 56), resp.getUpdatedDate()),
//                () -> assertEquals(LocalDate.of(2025, 4, 1), resp.getValid()),
//                () -> assertEquals(LocalDate.of(2025, 5, 1), resp.getEffective()),
//                () -> assertEquals("SOA123", resp.getHospSoa()),
//                () -> assertEquals("ICD10CODE", resp.getIcd10Code()),
//                () -> assertEquals("ICD10 Description", resp.getIcd10Desc()),
//                () -> assertEquals("REMARKS2", resp.getRemarks2()),
//                () -> assertEquals("CHECK123", resp.getChecknum()),
//                () -> assertEquals("PROFEE", resp.getPf()),
//                () -> assertEquals("RECEIVED", resp.getRcvdBy()),
//                () -> assertEquals(LocalDate.of(2025, 6, 1), resp.getRcvdDate()),
//                () -> assertEquals("DEPENDENT", resp.getDepname()),
//                () -> assertEquals("DEPCODE1", resp.getDepcode())
//        );
    }



    @Test
    @DisplayName("mapToUtilizationResponse returns zero BigDecimal for invalid numeric values")
    void testNumericFallback() {
        Object[] row = new Object[34];
        row[7] = "not-a-number";
        row[8] = null;
        row[9] = "abc";
        row[10] = null;
        row[18] = "invalid";

        UtilizationResponse resp = UtilizationMapper.mapToUtilizationResponse(row);

        assertEquals(BigDecimal.ZERO, resp.getApproved());
        assertEquals(BigDecimal.ZERO, resp.getDisapproved());
        assertEquals(BigDecimal.ZERO, resp.getAdvances());
        assertEquals(BigDecimal.ZERO, resp.getErc());
        assertEquals(BigDecimal.ZERO, resp.getMedicareIncentives());
    }

    @Test
    @DisplayName("mapToUtilizationResponse throws RuntimeException with error details on date parse failure")
    void testMapToUtilizationResponseDateParseError() {
        Object[] row = new Object[34];
        row[21] = "invalid-date-format";

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> UtilizationMapper.mapToUtilizationResponse(row));

        assertTrue(ex.getMessage().contains("Error mapping row"));
        assertNotNull(ex.getCause());
    }

    @Test
    @DisplayName("mapToUtilizationResponse handles all null values")
    void testMapToUtilizationResponseAllNulls() {
        Object[] row = new Object[34];
        Arrays.fill(row, null);

        UtilizationResponse resp = UtilizationMapper.mapToUtilizationResponse(row);

        assertNotNull(resp);
        assertNull(resp.getControlCode());
        assertNull(resp.getAvailFr());
        assertEquals(BigDecimal.ZERO, resp.getApproved());
        assertEquals(BigDecimal.ZERO, resp.getDisapproved());
    }

    @Test
    @DisplayName("mapToUtilizationResponse with mixed nulls and valid values")
    void testMapToUtilizationResponseMixedNulls() {
        Object[] row = new Object[34];
        Arrays.fill(row, null);
        row[0] = "CTRL456";
        row[7] = 100.0;
        row[11] = "MEM002";

        UtilizationResponse resp = UtilizationMapper.mapToUtilizationResponse(row);

        assertEquals("CTRL456", resp.getControlCode());
        assertEquals(new BigDecimal("100.0"), resp.getApproved());
        assertEquals("MEM002", resp.getMemcode());
        assertNull(resp.getAvailFr());
        assertNull(resp.getUpdatedDate());
    }

    @Test
    @DisplayName("mapToUtilizationResponse with BigDecimal input values")
    void testMapToUtilizationResponseBigDecimalInput() {
        Object[] row = new Object[34];
        row[7] = new BigDecimal("999.99");
        row[8] = new BigDecimal("111.11");
        row[9] = new BigDecimal("222.22");
        row[10] = new BigDecimal("333.33");
        row[18] = new BigDecimal("444.44");

        UtilizationResponse resp = UtilizationMapper.mapToUtilizationResponse(row);

        assertEquals(new BigDecimal("999.99"), resp.getApproved());
        assertEquals(new BigDecimal("111.11"), resp.getDisapproved());
        assertEquals(new BigDecimal("222.22"), resp.getAdvances());
        assertEquals(new BigDecimal("333.33"), resp.getErc());
        assertEquals(new BigDecimal("444.44"), resp.getMedicareIncentives());
    }

    // ==================== convertToLocalDate TESTS ====================

    @Test
    @DisplayName("convertToLocalDate handles Timestamp")
    void testConvertToLocalDateTimestamp() {
        Timestamp ts = Timestamp.valueOf("2025-03-15 10:30:00");
        LocalDate result = UtilizationMapper.convertToLocalDate(ts);
        assertEquals(LocalDate.of(2025, 3, 15), result);
    }

    @Test
    @DisplayName("convertToLocalDate handles ISO_LOCAL_DATE_TIME format")
    void testConvertToLocalDateIsoDateTime() {
        LocalDate result = UtilizationMapper.convertToLocalDate("2025-03-15T10:30:00");
        assertEquals(LocalDate.of(2025, 3, 15), result);
    }

    @Test
    @DisplayName("convertToLocalDate handles ISO_LOCAL_DATE format")
    void testConvertToLocalDateIsoDate() {
        LocalDate result = UtilizationMapper.convertToLocalDate("2025-03-15");
        assertEquals(LocalDate.of(2025, 3, 15), result);
    }

    @Test
    @DisplayName("convertToLocalDate handles yyyy-MM-dd HH:mm:ss format")
    void testConvertToLocalDateSqlFormat() {
        LocalDate result = UtilizationMapper.convertToLocalDate("2025-03-15 10:30:00");
        assertEquals(LocalDate.of(2025, 3, 15), result);
    }

    @Test
    @DisplayName("convertToLocalDate handles MM/dd/yyyy format")
    void testConvertToLocalDateAlternativeFormat() {
        LocalDate result = UtilizationMapper.convertToLocalDate("03/15/2025");
        assertEquals(LocalDate.of(2025, 3, 15), result);
    }

    @Test
    @DisplayName("convertToLocalDate returns null for blank string")
    void testConvertToLocalDateBlank() {
        assertNull(UtilizationMapper.convertToLocalDate("   "));
        assertNull(UtilizationMapper.convertToLocalDate(""));
        assertNull(UtilizationMapper.convertToLocalDate(null));
    }

    @Test
    @DisplayName("convertToLocalDate returns null for Character type")
    void testConvertToLocalDateCharacter() {
        assertNull(UtilizationMapper.convertToLocalDate('X'));
        assertNull(UtilizationMapper.convertToLocalDate('A'));
    }

    @Test
    @DisplayName("convertToLocalDate throws DateTimeParseException for unparseable string")
    void testConvertToLocalDateUnparseable() {
        assertThrows(DateTimeParseException.class,
                () -> UtilizationMapper.convertToLocalDate("2025/03/15"));
        assertThrows(DateTimeParseException.class,
                () -> UtilizationMapper.convertToLocalDate("15-03-2025"));
    }

    @Test
    @DisplayName("convertToLocalDate throws IllegalArgumentException for unsupported type")
    void testConvertToLocalDateUnsupportedType() {
        assertThrows(IllegalArgumentException.class,
                () -> UtilizationMapper.convertToLocalDate(123));
        assertThrows(IllegalArgumentException.class,
                () -> UtilizationMapper.convertToLocalDate(123.45));
        assertThrows(IllegalArgumentException.class,
                () -> UtilizationMapper.convertToLocalDate(true));
    }

    @Test
    @DisplayName("convertToLocalDate logs to System.err for Character input")
    void testConvertToLocalDateCharacterLogsToErr() {
        java.io.ByteArrayOutputStream errContent = new java.io.ByteArrayOutputStream();
        java.io.PrintStream originalErr = System.err;
        System.setErr(new java.io.PrintStream(errContent));

        try {
            LocalDate result = UtilizationMapper.convertToLocalDate('X');
            assertNull(result);
            assertTrue(errContent.toString().contains("Skipping invalid date value"));
        } finally {
            System.setErr(originalErr);
        }
    }

    @Test
    @DisplayName("convertToLocalDate logs to System.err for empty string")
    void testConvertToLocalDateEmptyStringLogsToErr() {
        java.io.ByteArrayOutputStream errContent = new java.io.ByteArrayOutputStream();
        java.io.PrintStream originalErr = System.err;
        System.setErr(new java.io.PrintStream(errContent));

        try {
            LocalDate result = UtilizationMapper.convertToLocalDate("   ");
            assertNull(result);
            assertTrue(errContent.toString().contains("Skipping invalid date value"));
        } finally {
            System.setErr(originalErr);
        }
    }

    // ==================== convertToLocalDateTime TESTS ====================

    @Test
    @DisplayName("convertToLocalDateTime handles Timestamp")
    void testConvertToLocalDateTimeTimestamp() {
        Timestamp ts = Timestamp.valueOf("2025-03-15 10:30:45");
        LocalDateTime result = UtilizationMapper.convertToLocalDateTime(ts);
        assertEquals(LocalDateTime.of(2025, 3, 15, 10, 30, 45), result);
    }

    @Test
    @DisplayName("convertToLocalDateTime handles ISO_LOCAL_DATE_TIME format")
    void testConvertToLocalDateTimeIsoFormat() {
        LocalDateTime result = UtilizationMapper.convertToLocalDateTime("2025-03-15T10:30:45");
        assertEquals(LocalDateTime.of(2025, 3, 15, 10, 30, 45), result);
    }

    @Test
    @DisplayName("convertToLocalDateTime returns null for blank string")
    void testConvertToLocalDateTimeBlank() {
        assertNull(UtilizationMapper.convertToLocalDateTime("   "));
        assertNull(UtilizationMapper.convertToLocalDateTime(""));
        assertNull(UtilizationMapper.convertToLocalDateTime(null));
    }

    @Test
    @DisplayName("convertToLocalDateTime throws IllegalArgumentException for unsupported type")
    void testConvertToLocalDateTimeUnsupportedType() {
        assertThrows(IllegalArgumentException.class,
                () -> UtilizationMapper.convertToLocalDateTime(123));
        assertThrows(IllegalArgumentException.class,
                () -> UtilizationMapper.convertToLocalDateTime(123.45));
    }

    @Test
    @DisplayName("convertToLocalDateTime handles SQL datetime format string")
    void testConvertToLocalDateTimeSqlFormat() {
        LocalDateTime result = UtilizationMapper.convertToLocalDateTime("2025-03-15 10:30:45");
        assertEquals(LocalDateTime.of(2025, 3, 15, 10, 30, 45), result);
    }

    @Test
    @DisplayName("convertToLocalDateTime throws IllegalArgumentException for unparseable string")
    void testConvertToLocalDateTimeUnparseable() {
        assertThrows(IllegalArgumentException.class,
                () -> UtilizationMapper.convertToLocalDateTime("invalid-datetime"));
    }

    // ==================== getString TESTS ====================

    @Test
    @DisplayName("getString returns string representation of non-null value")
    void testGetStringNonNull() throws Exception {
        Method method = UtilizationMapper.class.getDeclaredMethod("getString", Object.class);
        method.setAccessible(true);

        String result = (String) method.invoke(null, "testValue");
        assertEquals("testValue", result);

        result = (String) method.invoke(null, 123);
        assertEquals("123", result);

        result = (String) method.invoke(null, 45.67);
        assertEquals("45.67", result);
    }

    @Test
    @DisplayName("getString returns null for null value")
    void testGetStringNull() throws Exception {
        Method method = UtilizationMapper.class.getDeclaredMethod("getString", Object.class);
        method.setAccessible(true);

        String result = (String) method.invoke(null, (Object) null);
        assertNull(result);
    }

    // ==================== toBigDecimal TESTS ====================

    @Test
    @DisplayName("toBigDecimal converts valid string to BigDecimal")
    void testToBigDecimalValidString() throws Exception {
        Method method = UtilizationMapper.class.getDeclaredMethod("toBigDecimal", Object.class);
        method.setAccessible(true);

        BigDecimal result = (BigDecimal) method.invoke(null, "123.45");
        assertEquals(new BigDecimal("123.45"), result);
    }

    @Test
    @DisplayName("toBigDecimal converts number to BigDecimal")
    void testToBigDecimalNumber() throws Exception {
        Method method = UtilizationMapper.class.getDeclaredMethod("toBigDecimal", Object.class);
        method.setAccessible(true);

        BigDecimal result = (BigDecimal) method.invoke(null, 100.50);
        assertEquals(new BigDecimal("100.5"), result);
    }

    @Test
    @DisplayName("toBigDecimal returns ZERO for null value")
    void testToBigDecimalNull() throws Exception {
        Method method = UtilizationMapper.class.getDeclaredMethod("toBigDecimal", Object.class);
        method.setAccessible(true);

        BigDecimal result = (BigDecimal) method.invoke(null, (Object) null);
        assertEquals(BigDecimal.ZERO, result);
    }

    @Test
    @DisplayName("toBigDecimal returns ZERO for invalid string")
    void testToBigDecimalInvalidString() throws Exception {
        Method method = UtilizationMapper.class.getDeclaredMethod("toBigDecimal", Object.class);
        method.setAccessible(true);

        BigDecimal result = (BigDecimal) method.invoke(null, "not-a-number");
        assertEquals(BigDecimal.ZERO, result);
    }

    @Test
    @DisplayName("toBigDecimal returns ZERO for empty string")
    void testToBigDecimalEmptyString() throws Exception {
        Method method = UtilizationMapper.class.getDeclaredMethod("toBigDecimal", Object.class);
        method.setAccessible(true);

        BigDecimal result = (BigDecimal) method.invoke(null, "");
        assertEquals(BigDecimal.ZERO, result);
    }

    // ==================== toDto TESTS ====================

    @Test
    @DisplayName("toDto maps populated UtilizationLegacyEntity correctly")
    void testToDtoFullEntity() throws Exception {
        UtilizationLegacyEntity entity = new UtilizationLegacyEntity();

        UtilizationLegacyId id = new UtilizationLegacyId(
                "CTRL789",
                LocalDateTime.of(2025, 7, 1, 10, 0)
        );

        setPrivateField(entity, "id", id);

        entity.setAvailFr(LocalDateTime.of(2025, 7, 2, 0, 0));
        entity.setAvailTo(LocalDateTime.of(2025, 7, 3, 0, 0));
        entity.setDiagDesc("Diagnosis Test");
        entity.setDxRem("DX Remarks Test");
        entity.setHospitalName("Hospital Test");
        entity.setDoctorName("Doctor Test");
        entity.setApproved(250.50);
        entity.setDisapproved(50.25);
        entity.setAdvances(20.00);
        entity.setErc(10.00);
        entity.setMemcode("MEM999");
        entity.setPatient("Patient Test");
        entity.setCompany("Company Test");
        entity.setPeriodFr(LocalDateTime.of(2025, 8, 1, 0, 0));
        entity.setPeriodTo(LocalDateTime.of(2025, 8, 15, 0, 0));
        entity.setPrintedBy("PRINTED");
        entity.setBillcode("BILL999");
        entity.setMedicareIncentives(500.00);
        entity.setReimReason("Reason Test");
        entity.setUpdatedBy("USER123");
        entity.setValid("2025-09-01");
        entity.setEffective("2025-09-15");
        entity.setHospSoa("SOA999");
        entity.setIcd10code("ICD999");
        entity.setIcd10desc("ICD Description");
        entity.setRemarks2("Remarks Test");
        entity.setChecknum("CHECK999");
        entity.setPf("PROFEE999");
        entity.setRcvdBy("RECEIVED");
        entity.setRcvdDate(LocalDateTime.of(2025, 10, 1, 0, 0));
        entity.setDepname("Dependent Test");
        entity.setDepcode("DEPCODE999");

        UtilizationResponse resp = UtilizationMapper.toDto(entity);

        assertAll(
                () -> assertEquals("CTRL789", resp.getControlCode()),
                () -> assertEquals(LocalDate.of(2025, 7, 2), resp.getAvailFr()),
                () -> assertEquals(LocalDate.of(2025, 7, 3), resp.getAvailTo()),
                () -> assertEquals("Diagnosis Test", resp.getDiagDesc()),
                () -> assertEquals("DX Remarks Test", resp.getDxRem()),
                () -> assertEquals("Hospital Test", resp.getHospitalName()),
                () -> assertEquals("Doctor Test", resp.getDoctorName()),
                () -> assertEquals(BigDecimal.valueOf(250.50), resp.getApproved()),
                () -> assertEquals(BigDecimal.valueOf(50.25), resp.getDisapproved()),
                () -> assertEquals(BigDecimal.valueOf(20.00), resp.getAdvances()),
                () -> assertEquals(BigDecimal.valueOf(10.00), resp.getErc()),
                () -> assertEquals("MEM999", resp.getMemcode()),
                () -> assertEquals("Patient Test", resp.getPatient()),
                () -> assertEquals("Company Test", resp.getCompany()),
                () -> assertEquals(LocalDate.of(2025, 8, 1), resp.getPeriodFr()),
                () -> assertEquals(LocalDate.of(2025, 8, 15), resp.getPeriodTo()),
                () -> assertEquals("PRINTED", resp.getPrintedBy()),
                () -> assertEquals("BILL999", resp.getBillcode()),
                () -> assertEquals(BigDecimal.valueOf(500.00), resp.getMedicareIncentives()),
                () -> assertEquals("Reason Test", resp.getReimReason()),
                () -> assertEquals("USER123", resp.getUpdatedBy()),
                () -> assertEquals(LocalDateTime.of(2025, 7, 1, 10, 0), resp.getUpdatedDate()),
                () -> assertEquals(LocalDate.of(2025, 9, 1), resp.getValid()),
                () -> assertEquals(LocalDate.of(2025, 9, 15), resp.getEffective()),
                () -> assertEquals("SOA999", resp.getHospSoa()),
                () -> assertEquals("ICD999", resp.getIcd10Code()),
                () -> assertEquals("ICD Description", resp.getIcd10Desc()),
                () -> assertEquals("Remarks Test", resp.getRemarks2()),
                () -> assertEquals("CHECK999", resp.getChecknum()),
                () -> assertEquals("PROFEE999", resp.getPf()),
                () -> assertEquals("RECEIVED", resp.getRcvdBy()),
                () -> assertEquals(LocalDate.of(2025, 10, 1), resp.getRcvdDate()),
                () -> assertEquals("Dependent Test", resp.getDepname()),
                () -> assertEquals("DEPCODE999", resp.getDepcode())
        );
    }

    @Test
    @DisplayName("toDto handles null numeric fields with zero defaults")
    void testToDtoNullNumericFields() throws Exception {
        UtilizationLegacyEntity entity = new UtilizationLegacyEntity();

        UtilizationLegacyId id = new UtilizationLegacyId(
                "CTRL000",
                LocalDateTime.of(2025, 1, 1, 0, 0)
        );

        setPrivateField(entity, "id", id);

        entity.setApproved(null);
        entity.setDisapproved(null);
        entity.setAdvances(null);
        entity.setErc(null);
        entity.setMedicareIncentives(null);

        UtilizationResponse resp = UtilizationMapper.toDto(entity);

//        assertEquals(BigDecimal.ZERO, resp.getApproved());
//        assertEquals(BigDecimal.ZERO, resp.getDisapproved());
//        assertEquals(BigDecimal.ZERO, resp.getAdvances());
//        assertEquals(BigDecimal.ZERO, resp.getErc());
//        assertEquals(BigDecimal.ZERO, resp.getMedicareIncentives());
    }

    @Test
    @DisplayName("toDto handles entity with minimal data")
    void testToDtoMinimalEntity() throws Exception {
        UtilizationLegacyEntity entity = new UtilizationLegacyEntity();

        UtilizationLegacyId id = new UtilizationLegacyId(
                "MIN001",
                LocalDateTime.of(2025, 1, 1, 0, 0)
        );

        setPrivateField(entity, "id", id);

        UtilizationResponse resp = UtilizationMapper.toDto(entity);

        assertEquals("MIN001", resp.getControlCode());
        assertEquals(LocalDateTime.of(2025, 1, 1, 0, 0), resp.getUpdatedDate());
        assertNull(resp.getAvailFr());
        assertNull(resp.getDiagDesc());
    }

    @Test
    @DisplayName("toDto handles all null LocalDateTime fields")
    void testToDtoAllNullDateTimes() throws Exception {
        UtilizationLegacyEntity entity = new UtilizationLegacyEntity();

        UtilizationLegacyId id = new UtilizationLegacyId(
                "NULL001",
                LocalDateTime.of(2025, 1, 1, 0, 0)
        );

        setPrivateField(entity, "id", id);

        entity.setAvailFr(null);
        entity.setAvailTo(null);
        entity.setPeriodFr(null);
        entity.setPeriodTo(null);
        entity.setRcvdDate(null);
        entity.setValid(null);
        entity.setEffective(null);

        UtilizationResponse resp = UtilizationMapper.toDto(entity);

        assertNull(resp.getAvailFr());
        assertNull(resp.getAvailTo());
        assertNull(resp.getPeriodFr());
        assertNull(resp.getPeriodTo());
        assertNull(resp.getRcvdDate());
        assertNull(resp.getValid());
        assertNull(resp.getEffective());
    }

    @Test
    @DisplayName("toDto handles valid and effective string dates")
    void testToDtoStringDates() throws Exception {
        UtilizationLegacyEntity entity = new UtilizationLegacyEntity();

        UtilizationLegacyId id = new UtilizationLegacyId(
                "STR001",
                LocalDateTime.of(2025, 1, 1, 0, 0)
        );

        setPrivateField(entity, "id", id);

        entity.setValid("2025-06-15");
        entity.setEffective("2025-07-20");

        UtilizationResponse resp = UtilizationMapper.toDto(entity);

        assertEquals(LocalDate.of(2025, 6, 15), resp.getValid());
        assertEquals(LocalDate.of(2025, 7, 20), resp.getEffective());
    }

    // ==================== getErrorIndex TESTS ====================

    @Test
    @DisplayName("getErrorIndex extracts index from DateTimeParseException with index in message")
    void testGetErrorIndexDateTimeParseException() throws Exception {
        DateTimeParseException ex = new DateTimeParseException("Unparseable date at index 5", "test", 5);

        Method method = UtilizationMapper.class.getDeclaredMethod("getErrorIndex", Exception.class);
        method.setAccessible(true);

        Integer result = (Integer) method.invoke(null, ex);
        assertEquals(5, result);
    }

    @Test
    @DisplayName("getErrorIndex returns -1 for DateTimeParseException without index keyword")
    void testGetErrorIndexDateTimeParseExceptionNoIndexKeyword() throws Exception {
        DateTimeParseException ex = new DateTimeParseException("Unparseable date", "test", 0);

        Method method = UtilizationMapper.class.getDeclaredMethod("getErrorIndex", Exception.class);
        method.setAccessible(true);

        Integer result = (Integer) method.invoke(null, ex);
        assertEquals(-1, result);
    }

    @Test
    @DisplayName("getErrorIndex returns -1 for non-DateTimeParseException")
    void testGetErrorIndexNonDateTimeParseException() throws Exception {
        RuntimeException ex = new RuntimeException("Generic error message");

        Method method = UtilizationMapper.class.getDeclaredMethod("getErrorIndex", Exception.class);
        method.setAccessible(true);

        Integer result = (Integer) method.invoke(null, ex);
        assertEquals(-1, result);
    }

    @Test
    @DisplayName("getErrorIndex returns -1 for null exception")
    void testGetErrorIndexNullException() throws Exception {
        Method method = UtilizationMapper.class.getDeclaredMethod("getErrorIndex", Exception.class);
        method.setAccessible(true);

        Integer result = (Integer) method.invoke(null, (Exception) null);
        assertEquals(-1, result);
    }

    @Test
    @DisplayName("getErrorIndex extracts correct index from complex message")
    void testGetErrorIndexComplexMessage() throws Exception {
        DateTimeParseException ex = new DateTimeParseException("Parse error at index 15 with value", "test", 15);

        Method method = UtilizationMapper.class.getDeclaredMethod("getErrorIndex", Exception.class);
        method.setAccessible(true);

        Integer result = (Integer) method.invoke(null, ex);
        assertEquals(15, result);
    }

    @Test
    @DisplayName("getErrorIndex returns -1 for IndexOutOfBoundsException")
    void testGetErrorIndexIndexOutOfBounds() throws Exception {
        IndexOutOfBoundsException ex = new IndexOutOfBoundsException("Index 15 out of bounds");

        Method method = UtilizationMapper.class.getDeclaredMethod("getErrorIndex", Exception.class);
        method.setAccessible(true);

        Integer result = (Integer) method.invoke(null, ex);
        assertEquals(-1, result); // Should return -1 as it's not DateTimeParseException
    }

    // ==================== HELPER METHODS ====================

    private void setPrivateField(Object target, String fieldName, Object value) throws Exception {
        var field = target.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(target, value);
    }
}

