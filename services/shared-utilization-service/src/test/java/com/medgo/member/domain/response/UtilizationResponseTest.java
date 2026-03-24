package com.medgo.member.domain.response;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.function.BiConsumer;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

class UtilizationResponseTest {



    @Test
    @DisplayName("All getters and setters work correctly")
    void testAllGettersAndSetters() {
        UtilizationResponse response = new UtilizationResponse();

        LocalDate date = LocalDate.of(2025, 11, 15);
        LocalDateTime dateTime = LocalDateTime.of(2025, 11, 15, 10, 30);
        BigDecimal decimal = new BigDecimal("123.45");

        // Set all fields
        response.setControlCode("CTRL001");
        response.setAvailFr(date);
        response.setAvailTo(date.plusDays(1));
        response.setDiagDesc("Diagnosis Description");
        response.setDxRem("DX Remarks");
        response.setHospitalName("Test Hospital");
        response.setDoctorName("Dr. Smith");
        response.setApproved(decimal);
        response.setDisapproved(new BigDecimal("50.00"));
        response.setAdvances(new BigDecimal("25.00"));
        response.setErc(new BigDecimal("10.00"));
        response.setMemcode("MEM123");
        response.setPatient("John Doe");
        response.setCompany("Test Company");
        response.setPeriodFr(date);
        response.setPeriodTo(date.plusDays(30));
        response.setPrintedBy("Admin");
        response.setBillcode("BILL001");
        response.setMedicareIncentives(new BigDecimal("15.00"));
        response.setReimReason("Reimbursement Reason");
        response.setUpdatedBy("UpdateUser");
        response.setUpdatedDate(dateTime);
        response.setValid(date);
        response.setEffective(date);
        response.setHospSoa("SOA123");
        response.setIcd10Code("A00.0");
        response.setIcd10Desc("ICD10 Description");
        response.setRemarks2("Additional Remarks");
        response.setChecknum("CHK001");
        response.setPf("PF001");
        response.setRcvdBy("Receiver");
        response.setRcvdDate(date);
        response.setDepname("Department Name");
        response.setDepcode("DEP001");

        // Assert all fields
        assertAll(
            () -> assertEquals("CTRL001", response.getControlCode()),
            () -> assertEquals(date, response.getAvailFr()),
            () -> assertEquals(date.plusDays(1), response.getAvailTo()),
            () -> assertEquals("Diagnosis Description", response.getDiagDesc()),
            () -> assertEquals("DX Remarks", response.getDxRem()),
            () -> assertEquals("Test Hospital", response.getHospitalName()),
            () -> assertEquals("Dr. Smith", response.getDoctorName()),
            () -> assertEquals(decimal, response.getApproved()),
            () -> assertEquals(new BigDecimal("50.00"), response.getDisapproved()),
            () -> assertEquals(new BigDecimal("25.00"), response.getAdvances()),
            () -> assertEquals(new BigDecimal("10.00"), response.getErc()),
            () -> assertEquals("MEM123", response.getMemcode()),
            () -> assertEquals("John Doe", response.getPatient()),
            () -> assertEquals("Test Company", response.getCompany()),
            () -> assertEquals(date, response.getPeriodFr()),
            () -> assertEquals(date.plusDays(30), response.getPeriodTo()),
            () -> assertEquals("Admin", response.getPrintedBy()),
            () -> assertEquals("BILL001", response.getBillcode()),
            () -> assertEquals(new BigDecimal("15.00"), response.getMedicareIncentives()),
            () -> assertEquals("Reimbursement Reason", response.getReimReason()),
            () -> assertEquals("UpdateUser", response.getUpdatedBy()),
            () -> assertEquals(dateTime, response.getUpdatedDate()),
            () -> assertEquals(date, response.getValid()),
            () -> assertEquals(date, response.getEffective()),
            () -> assertEquals("SOA123", response.getHospSoa()),
            () -> assertEquals("A00.0", response.getIcd10Code()),
            () -> assertEquals("ICD10 Description", response.getIcd10Desc()),
            () -> assertEquals("Additional Remarks", response.getRemarks2()),
            () -> assertEquals("CHK001", response.getChecknum()),
            () -> assertEquals("PF001", response.getPf()),
            () -> assertEquals("Receiver", response.getRcvdBy()),
            () -> assertEquals(date, response.getRcvdDate()),
            () -> assertEquals("Department Name", response.getDepname()),
            () -> assertEquals("DEP001", response.getDepcode())
        );
    }

    @Test
    @DisplayName("Setters with null values")
    void testSettersWithNullValues() {
        UtilizationResponse response = createFullyPopulatedResponse();

        // Set all to null
        response.setControlCode(null);
        response.setAvailFr(null);
        response.setAvailTo(null);
        response.setDiagDesc(null);
        response.setDxRem(null);
        response.setHospitalName(null);
        response.setDoctorName(null);
        response.setApproved(null);
        response.setDisapproved(null);
        response.setAdvances(null);
        response.setErc(null);
        response.setMemcode(null);
        response.setPatient(null);
        response.setCompany(null);
        response.setPeriodFr(null);
        response.setPeriodTo(null);
        response.setPrintedBy(null);
        response.setBillcode(null);
        response.setMedicareIncentives(null);
        response.setReimReason(null);
        response.setUpdatedBy(null);
        response.setUpdatedDate(null);
        response.setValid(null);
        response.setEffective(null);
        response.setHospSoa(null);
        response.setIcd10Code(null);
        response.setIcd10Desc(null);
        response.setRemarks2(null);
        response.setChecknum(null);
        response.setPf(null);
        response.setRcvdBy(null);
        response.setRcvdDate(null);
        response.setDepname(null);
        response.setDepcode(null);

        // Assert all null
        assertAll(
            () -> assertNull(response.getControlCode()),
            () -> assertNull(response.getAvailFr()),
            () -> assertNull(response.getAvailTo()),
            () -> assertNull(response.getDiagDesc()),
            () -> assertNull(response.getDxRem()),
            () -> assertNull(response.getHospitalName()),
            () -> assertNull(response.getDoctorName()),
            () -> assertNull(response.getApproved()),
            () -> assertNull(response.getDisapproved()),
            () -> assertNull(response.getAdvances()),
            () -> assertNull(response.getErc()),
            () -> assertNull(response.getMemcode()),
            () -> assertNull(response.getPatient()),
            () -> assertNull(response.getCompany()),
            () -> assertNull(response.getPeriodFr()),
            () -> assertNull(response.getPeriodTo()),
            () -> assertNull(response.getPrintedBy()),
            () -> assertNull(response.getBillcode()),
            () -> assertNull(response.getMedicareIncentives()),
            () -> assertNull(response.getReimReason()),
            () -> assertNull(response.getUpdatedBy()),
            () -> assertNull(response.getUpdatedDate()),
            () -> assertNull(response.getValid()),
            () -> assertNull(response.getEffective()),
            () -> assertNull(response.getHospSoa()),
            () -> assertNull(response.getIcd10Code()),
            () -> assertNull(response.getIcd10Desc()),
            () -> assertNull(response.getRemarks2()),
            () -> assertNull(response.getChecknum()),
            () -> assertNull(response.getPf()),
            () -> assertNull(response.getRcvdBy()),
            () -> assertNull(response.getRcvdDate()),
            () -> assertNull(response.getDepname()),
            () -> assertNull(response.getDepcode())
        );
    }

    // ========== equals() Tests ==========

    @Test
    @DisplayName("equals - same object returns true")
    void testEqualsSameObject() {
        UtilizationResponse response = createFullyPopulatedResponse();
        assertEquals(response, response);
    }

    @Test
    @DisplayName("equals - identical values returns true")
    void testEqualsIdenticalValues() {
        UtilizationResponse response1 = createFullyPopulatedResponse();
        UtilizationResponse response2 = createFullyPopulatedResponse();
        assertEquals(response1, response2);
        assertEquals(response2, response1); // symmetric
    }

    @Test
    @DisplayName("equals - null object returns false")
    void testEqualsNullObject() {
        UtilizationResponse response = createFullyPopulatedResponse();
        assertNotEquals(response, null);
    }

    @Test
    @DisplayName("equals - different type returns false")
    void testEqualsDifferentType() {
        UtilizationResponse response = createFullyPopulatedResponse();
        assertNotEquals(response, "not a UtilizationResponse");
        assertNotEquals(response, new Object());
    }

    @Test
    @DisplayName("equals - both all fields null")
    void testEqualsAllFieldsNull() {
        UtilizationResponse response1 = new UtilizationResponse();
        UtilizationResponse response2 = new UtilizationResponse();
        assertEquals(response1, response2);
    }

    @ParameterizedTest
    @MethodSource("fieldDifferences")
    @DisplayName("equals - difference in each field returns false")
    void testEqualsDifferentFields(BiConsumer<UtilizationResponse, UtilizationResponse> differentiator) {
        UtilizationResponse response1 = createFullyPopulatedResponse();
        UtilizationResponse response2 = createFullyPopulatedResponse();

        differentiator.accept(response1, response2);
        assertNotEquals(response1, response2);
    }

    static Stream<BiConsumer<UtilizationResponse, UtilizationResponse>> fieldDifferences() {
        return Stream.of(
            (r1, r2) -> r2.setControlCode("DIFFERENT"),
            (r1, r2) -> r2.setAvailFr(r1.getAvailFr().plusDays(1)),
            (r1, r2) -> r2.setAvailTo(r1.getAvailTo().plusDays(1)),
            (r1, r2) -> r2.setDiagDesc("Different Diagnosis"),
            (r1, r2) -> r2.setDxRem("Different Remarks"),
            (r1, r2) -> r2.setHospitalName("Different Hospital"),
            (r1, r2) -> r2.setDoctorName("Dr. Different"),
            (r1, r2) -> r2.setApproved(new BigDecimal("999.99")),
            (r1, r2) -> r2.setDisapproved(new BigDecimal("888.88")),
            (r1, r2) -> r2.setAdvances(new BigDecimal("777.77")),
            (r1, r2) -> r2.setErc(new BigDecimal("666.66")),
            (r1, r2) -> r2.setMemcode("DIFFMEM"),
            (r1, r2) -> r2.setPatient("Jane Doe"),
            (r1, r2) -> r2.setCompany("Different Company"),
            (r1, r2) -> r2.setPeriodFr(r1.getPeriodFr().plusDays(5)),
            (r1, r2) -> r2.setPeriodTo(r1.getPeriodTo().plusDays(5)),
            (r1, r2) -> r2.setPrintedBy("DifferentUser"),
            (r1, r2) -> r2.setBillcode("DIFFBILL"),
            (r1, r2) -> r2.setMedicareIncentives(new BigDecimal("555.55")),
            (r1, r2) -> r2.setReimReason("Different Reason"),
            (r1, r2) -> r2.setUpdatedBy("DifferentUpdater"),
            (r1, r2) -> r2.setUpdatedDate(r1.getUpdatedDate().plusHours(1)),
            (r1, r2) -> r2.setValid(r1.getValid().plusDays(1)),
            (r1, r2) -> r2.setEffective(r1.getEffective().plusDays(1)),
            (r1, r2) -> r2.setHospSoa("DIFFSOA"),
            (r1, r2) -> r2.setIcd10Code("B99.9"),
            (r1, r2) -> r2.setIcd10Desc("Different ICD10"),
            (r1, r2) -> r2.setRemarks2("Different Remarks2"),
            (r1, r2) -> r2.setChecknum("DIFFCHK"),
            (r1, r2) -> r2.setPf("DIFFPF"),
            (r1, r2) -> r2.setRcvdBy("DifferentReceiver"),
            (r1, r2) -> r2.setRcvdDate(r1.getRcvdDate().plusDays(1)),
            (r1, r2) -> r2.setDepname("Different Department"),
            (r1, r2) -> r2.setDepcode("DIFFDEP")
        );
    }

    @Test
    @DisplayName("equals - null vs non-null field")
    void testEqualsNullVsNonNull() {
        UtilizationResponse response1 = createFullyPopulatedResponse();
        UtilizationResponse response2 = createFullyPopulatedResponse();

        response2.setControlCode(null);
        assertNotEquals(response1, response2);

        response1.setControlCode(null);
        response2.setControlCode("CTRL001");
        assertNotEquals(response1, response2);
    }

    @Test
    @DisplayName("equals - transitive property")
    void testEqualsTransitive() {
        UtilizationResponse r1 = createFullyPopulatedResponse();
        UtilizationResponse r2 = createFullyPopulatedResponse();
        UtilizationResponse r3 = createFullyPopulatedResponse();

        assertTrue(r1.equals(r2) && r2.equals(r3) && r1.equals(r3));
    }

    @Test
    @DisplayName("equals - consistent across multiple invocations")
    void testEqualsConsistent() {
        UtilizationResponse r1 = createFullyPopulatedResponse();
        UtilizationResponse r2 = createFullyPopulatedResponse();

        for (int i = 0; i < 5; i++) {
            assertEquals(r1, r2);
        }
    }

    // ========== hashCode() Tests ==========

    @Test
    @DisplayName("hashCode - equal objects have equal hashCodes")
    void testHashCodeEqualObjects() {
        UtilizationResponse r1 = createFullyPopulatedResponse();
        UtilizationResponse r2 = createFullyPopulatedResponse();
        assertEquals(r1.hashCode(), r2.hashCode());
    }

    @Test
    @DisplayName("hashCode - consistent across multiple invocations")
    void testHashCodeConsistent() {
        UtilizationResponse response = createFullyPopulatedResponse();
        int hashCode = response.hashCode();

        for (int i = 0; i < 5; i++) {
            assertEquals(hashCode, response.hashCode());
        }
    }

    @Test
    @DisplayName("hashCode - different when field changes")
    void testHashCodeDifferentOnChange() {
        UtilizationResponse response = createFullyPopulatedResponse();
        int originalHashCode = response.hashCode();

        response.setControlCode("CHANGED");
        assertNotEquals(originalHashCode, response.hashCode());
    }

    @Test
    @DisplayName("hashCode - handles null fields")
    void testHashCodeNullFields() {
        UtilizationResponse r1 = new UtilizationResponse();
        UtilizationResponse r2 = new UtilizationResponse();
        assertEquals(r1.hashCode(), r2.hashCode());
    }

    @ParameterizedTest
    @MethodSource("fieldDifferences")
    @DisplayName("hashCode - different for each field difference")
    void testHashCodeDifferentFields(BiConsumer<UtilizationResponse, UtilizationResponse> differentiator) {
        UtilizationResponse r1 = createFullyPopulatedResponse();
        UtilizationResponse r2 = createFullyPopulatedResponse();

        differentiator.accept(r1, r2);
        assertNotEquals(r1.hashCode(), r2.hashCode());
    }

    // ========== toString() Tests ==========

    @Test
    @DisplayName("toString - contains class name")
    void testToStringContainsClassName() {
        UtilizationResponse response = createFullyPopulatedResponse();
        String toString = response.toString();
        assertTrue(toString.contains("UtilizationResponse"));
    }

    @Test
    @DisplayName("toString - contains field values")
    void testToStringContainsFieldValues() {
        UtilizationResponse response = createFullyPopulatedResponse();
        String toString = response.toString();

        assertTrue(toString.contains("CTRL001"));
        assertTrue(toString.contains("Test Hospital"));
        assertTrue(toString.contains("John Doe"));
        assertTrue(toString.contains("Test Company"));
    }

    @Test
    @DisplayName("toString - handles null fields")
    void testToStringWithNullFields() {
        UtilizationResponse response = new UtilizationResponse();
        String toString = response.toString();

        assertNotNull(toString);
        assertTrue(toString.contains("UtilizationResponse"));
        assertTrue(toString.contains("null"));
    }

    @Test
    @DisplayName("toString - not null or empty")
    void testToStringNotNullOrEmpty() {
        UtilizationResponse response = createFullyPopulatedResponse();
        String toString = response.toString();

        assertNotNull(toString);
        assertFalse(toString.isEmpty());
    }

    // ========== Edge Cases ==========

    @Test
    @DisplayName("Edge case - BigDecimal precision")
    void testBigDecimalPrecision() {
        UtilizationResponse response = new UtilizationResponse();
        BigDecimal precise = new BigDecimal("123.456789");

        response.setApproved(precise);
        assertEquals(precise, response.getApproved());
        assertEquals(0, precise.compareTo(response.getApproved()));
    }

    @Test
    @DisplayName("Edge case - empty strings")
    void testEmptyStrings() {
        UtilizationResponse response = new UtilizationResponse();

        response.setControlCode("");
        response.setDiagDesc("");
        response.setPatient("");
        response.setCompany("");

        assertEquals("", response.getControlCode());
        assertEquals("", response.getDiagDesc());
        assertEquals("", response.getPatient());
        assertEquals("", response.getCompany());
    }

    @Test
    @DisplayName("Edge case - whitespace strings")
    void testWhitespaceStrings() {
        UtilizationResponse response = new UtilizationResponse();

        response.setControlCode("   ");
        response.setPatient("   ");
        response.setCompany("   ");

        assertEquals("   ", response.getControlCode());
        assertEquals("   ", response.getPatient());
        assertEquals("   ", response.getCompany());
    }

    @Test
    @DisplayName("Edge case - very long strings")
    void testVeryLongStrings() {
        UtilizationResponse response = new UtilizationResponse();
        String longString = "A".repeat(1000);

        response.setControlCode(longString);
        response.setDiagDesc(longString);

        assertEquals(longString, response.getControlCode());
        assertEquals(longString, response.getDiagDesc());
    }

    @Test
    @DisplayName("Edge case - special characters")
    void testSpecialCharacters() {
        UtilizationResponse response = new UtilizationResponse();
        String special = "Test@#$%^&*()_+-=[]{}|;:',.<>?/~`";

        response.setControlCode(special);
        response.setPatient(special);

        assertEquals(special, response.getControlCode());
        assertEquals(special, response.getPatient());
    }

    @Test
    @DisplayName("Multiple setter invocations overwrite values")
    void testMultipleSetterInvocations() {
        UtilizationResponse response = new UtilizationResponse();

        response.setControlCode("First");
        assertEquals("First", response.getControlCode());

        response.setControlCode("Second");
        assertEquals("Second", response.getControlCode());

        response.setControlCode("Third");
        assertEquals("Third", response.getControlCode());
    }

    @Test
    @DisplayName("BigDecimal zero values")
    void testBigDecimalZeroValues() {
        UtilizationResponse response = new UtilizationResponse();

        response.setApproved(BigDecimal.ZERO);
        response.setDisapproved(BigDecimal.ZERO);

        assertEquals(BigDecimal.ZERO, response.getApproved());
        assertEquals(BigDecimal.ZERO, response.getDisapproved());
    }

    @Test
    @DisplayName("BigDecimal negative values")
    void testBigDecimalNegativeValues() {
        UtilizationResponse response = new UtilizationResponse();
        BigDecimal negative = new BigDecimal("-100.50");

        response.setApproved(negative);
        assertEquals(negative, response.getApproved());
    }

    @Test
    @DisplayName("LocalDate boundary values")
    void testLocalDateBoundaryValues() {
        UtilizationResponse response = new UtilizationResponse();

        LocalDate minDate = LocalDate.MIN;
        LocalDate maxDate = LocalDate.MAX;

        response.setAvailFr(minDate);
        response.setAvailTo(maxDate);

        assertEquals(minDate, response.getAvailFr());
        assertEquals(maxDate, response.getAvailTo());
    }

    @Test
    @DisplayName("LocalDateTime boundary values")
    void testLocalDateTimeBoundaryValues() {
        UtilizationResponse response = new UtilizationResponse();

        LocalDateTime minDateTime = LocalDateTime.MIN;
        LocalDateTime maxDateTime = LocalDateTime.MAX;

        response.setUpdatedDate(minDateTime);
        assertEquals(minDateTime, response.getUpdatedDate());

        response.setUpdatedDate(maxDateTime);
        assertEquals(maxDateTime, response.getUpdatedDate());
    }

    // ========== Helper Methods ==========

    private UtilizationResponse createFullyPopulatedResponse() {
        UtilizationResponse response = new UtilizationResponse();

        LocalDate date = LocalDate.of(2025, 11, 15);
        LocalDateTime dateTime = LocalDateTime.of(2025, 11, 15, 10, 30);

        response.setControlCode("CTRL001");
        response.setAvailFr(date);
        response.setAvailTo(date.plusDays(1));
        response.setDiagDesc("Diagnosis Description");
        response.setDxRem("DX Remarks");
        response.setHospitalName("Test Hospital");
        response.setDoctorName("Dr. Smith");
        response.setApproved(new BigDecimal("123.45"));
        response.setDisapproved(new BigDecimal("50.00"));
        response.setAdvances(new BigDecimal("25.00"));
        response.setErc(new BigDecimal("10.00"));
        response.setMemcode("MEM123");
        response.setPatient("John Doe");
        response.setCompany("Test Company");
        response.setPeriodFr(date);
        response.setPeriodTo(date.plusDays(30));
        response.setPrintedBy("Admin");
        response.setBillcode("BILL001");
        response.setMedicareIncentives(new BigDecimal("15.00"));
        response.setReimReason("Reimbursement Reason");
        response.setUpdatedBy("UpdateUser");
        response.setUpdatedDate(dateTime);
        response.setValid(date);
        response.setEffective(date);
        response.setHospSoa("SOA123");
        response.setIcd10Code("A00.0");
        response.setIcd10Desc("ICD10 Description");
        response.setRemarks2("Additional Remarks");
        response.setChecknum("CHK001");
        response.setPf("PF001");
        response.setRcvdBy("Receiver");
        response.setRcvdDate(date);
        response.setDepname("Department Name");
        response.setDepcode("DEP001");

        return response;
    }
}

