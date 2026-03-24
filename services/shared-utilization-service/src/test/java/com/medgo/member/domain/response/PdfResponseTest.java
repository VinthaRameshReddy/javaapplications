package com.medgo.member.domain.response;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PdfResponseTest {



    @Test
    @DisplayName("Constructor and getters/setters work correctly")
    void testConstructorAndGettersSetters() {
        PdfResponse response = new PdfResponse("base64String", "fileName.pdf");

        assertEquals("base64String", response.getBase64File());
        assertEquals("fileName.pdf", response.getFileName());

        response.setBase64File("newBase64String");
        response.setFileName("newFileName.pdf");

        assertEquals("newBase64String", response.getBase64File());
        assertEquals("newFileName.pdf", response.getFileName());
    }

    @Test
    @DisplayName("Constructor with null values")
    void testConstructorWithNullValues() {
        PdfResponse response = new PdfResponse(null, null);
        assertNull(response.getBase64File());
        assertNull(response.getFileName());
    }

    @Test
    @DisplayName("Setters can set null values")
    void testSettersWithNull() {
        PdfResponse response = new PdfResponse("base64", "file.pdf");
        response.setBase64File(null);
        response.setFileName(null);
        assertNull(response.getBase64File());
        assertNull(response.getFileName());
    }

    // ========== equals() Tests ==========

    @Test
    @DisplayName("equals - same object returns true")
    void testEqualsSameObject() {
        PdfResponse response = new PdfResponse("base64", "file.pdf");
        assertEquals(response, response);
    }

    @Test
    @DisplayName("equals - identical values returns true")
    void testEqualsIdenticalValues() {
        PdfResponse response1 = new PdfResponse("base64", "file.pdf");
        PdfResponse response2 = new PdfResponse("base64", "file.pdf");
        assertEquals(response1, response2);
        assertEquals(response2, response1); // symmetric
    }

    @Test
    @DisplayName("equals - different base64File returns false")
    void testEqualsDifferentBase64File() {
        PdfResponse response1 = new PdfResponse("base64_1", "file.pdf");
        PdfResponse response2 = new PdfResponse("base64_2", "file.pdf");
        assertNotEquals(response1, response2);
    }

    @Test
    @DisplayName("equals - different fileName returns false")
    void testEqualsDifferentFileName() {
        PdfResponse response1 = new PdfResponse("base64", "file1.pdf");
        PdfResponse response2 = new PdfResponse("base64", "file2.pdf");
        assertNotEquals(response1, response2);
    }

    @Test
    @DisplayName("equals - both fields different returns false")
    void testEqualsBothFieldsDifferent() {
        PdfResponse response1 = new PdfResponse("base64_1", "file1.pdf");
        PdfResponse response2 = new PdfResponse("base64_2", "file2.pdf");
        assertNotEquals(response1, response2);
    }

    @Test
    @DisplayName("equals - null object returns false")
    void testEqualsNullObject() {
        PdfResponse response = new PdfResponse("base64", "file.pdf");
        assertNotEquals(response, null);
    }

    @Test
    @DisplayName("equals - different type returns false")
    void testEqualsDifferentType() {
        PdfResponse response = new PdfResponse("base64", "file.pdf");
        assertNotEquals(response, "not a PdfResponse");
        assertNotEquals(response, new Object());
    }

    @Test
    @DisplayName("equals - both null base64File")
    void testEqualsNullBase64File() {
        PdfResponse response1 = new PdfResponse(null, "file.pdf");
        PdfResponse response2 = new PdfResponse(null, "file.pdf");
        assertEquals(response1, response2);
    }

    @Test
    @DisplayName("equals - one null base64File, one not")
    void testEqualsOneNullBase64File() {
        PdfResponse response1 = new PdfResponse(null, "file.pdf");
        PdfResponse response2 = new PdfResponse("base64", "file.pdf");
        assertNotEquals(response1, response2);
        assertNotEquals(response2, response1);
    }

    @Test
    @DisplayName("equals - both null fileName")
    void testEqualsNullFileName() {
        PdfResponse response1 = new PdfResponse("base64", null);
        PdfResponse response2 = new PdfResponse("base64", null);
        assertEquals(response1, response2);
    }

    @Test
    @DisplayName("equals - one null fileName, one not")
    void testEqualsOneNullFileName() {
        PdfResponse response1 = new PdfResponse("base64", null);
        PdfResponse response2 = new PdfResponse("base64", "file.pdf");
        assertNotEquals(response1, response2);
        assertNotEquals(response2, response1);
    }

    @Test
    @DisplayName("equals - both all fields null")
    void testEqualsAllFieldsNull() {
        PdfResponse response1 = new PdfResponse(null, null);
        PdfResponse response2 = new PdfResponse(null, null);
        assertEquals(response1, response2);
    }

    @Test
    @DisplayName("equals - transitive property")
    void testEqualsTransitive() {
        PdfResponse response1 = new PdfResponse("base64", "file.pdf");
        PdfResponse response2 = new PdfResponse("base64", "file.pdf");
        PdfResponse response3 = new PdfResponse("base64", "file.pdf");
        assertEquals(response1, response2);
        assertEquals(response2, response3);
        assertEquals(response1, response3);
    }

    @Test
    @DisplayName("equals - consistent across multiple invocations")
    void testEqualsConsistent() {
        PdfResponse response1 = new PdfResponse("base64", "file.pdf");
        PdfResponse response2 = new PdfResponse("base64", "file.pdf");
        for (int i = 0; i < 5; i++) {
            assertEquals(response1, response2);
        }
    }

    // ========== hashCode() Tests ==========

    @Test
    @DisplayName("hashCode - equal objects have equal hashCodes")
    void testHashCodeEqualObjects() {
        PdfResponse response1 = new PdfResponse("base64", "file.pdf");
        PdfResponse response2 = new PdfResponse("base64", "file.pdf");
        assertEquals(response1.hashCode(), response2.hashCode());
    }

    @Test
    @DisplayName("hashCode - different base64File produces different hashCode")
    void testHashCodeDifferentBase64File() {
        PdfResponse response1 = new PdfResponse("base64_1", "file.pdf");
        PdfResponse response2 = new PdfResponse("base64_2", "file.pdf");
        assertNotEquals(response1.hashCode(), response2.hashCode());
    }

    @Test
    @DisplayName("hashCode - different fileName produces different hashCode")
    void testHashCodeDifferentFileName() {
        PdfResponse response1 = new PdfResponse("base64", "file1.pdf");
        PdfResponse response2 = new PdfResponse("base64", "file2.pdf");
        assertNotEquals(response1.hashCode(), response2.hashCode());
    }

    @Test
    @DisplayName("hashCode - consistent across multiple invocations")
    void testHashCodeConsistent() {
        PdfResponse response = new PdfResponse("base64", "file.pdf");
        int hashCode = response.hashCode();
        for (int i = 0; i < 5; i++) {
            assertEquals(hashCode, response.hashCode());
        }
    }

    @Test
    @DisplayName("hashCode - null fields handled correctly")
    void testHashCodeNullFields() {
        PdfResponse response1 = new PdfResponse(null, null);
        PdfResponse response2 = new PdfResponse(null, null);
        assertEquals(response1.hashCode(), response2.hashCode());
    }

    @Test
    @DisplayName("hashCode - changes when field is modified")
    void testHashCodeChangesOnModification() {
        PdfResponse response = new PdfResponse("base64", "file.pdf");
        int originalHashCode = response.hashCode();

        response.setBase64File("newBase64");
        assertNotEquals(originalHashCode, response.hashCode());

        int newHashCode = response.hashCode();
        response.setFileName("newFile.pdf");
        assertNotEquals(newHashCode, response.hashCode());
    }

    @Test
    @DisplayName("hashCode - partial null values")
    void testHashCodePartialNullValues() {
        PdfResponse response1 = new PdfResponse(null, "file.pdf");
        PdfResponse response2 = new PdfResponse(null, "file.pdf");
        assertEquals(response1.hashCode(), response2.hashCode());

        PdfResponse response3 = new PdfResponse("base64", null);
        PdfResponse response4 = new PdfResponse("base64", null);
        assertEquals(response3.hashCode(), response4.hashCode());
    }

    // ========== toString() Tests ==========

    @Test
    @DisplayName("toString - contains class name")
    void testToStringContainsClassName() {
        PdfResponse response = new PdfResponse("base64", "file.pdf");
        String toString = response.toString();
        assertTrue(toString.contains("PdfResponse"));
    }

    @Test
    @DisplayName("toString - contains field names and values")
    void testToStringContainsFieldsAndValues() {
        PdfResponse response = new PdfResponse("base64String", "fileName.pdf");
        String toString = response.toString();

        assertTrue(toString.contains("base64File"));
        assertTrue(toString.contains("base64String"));
        assertTrue(toString.contains("fileName"));
        assertTrue(toString.contains("fileName.pdf"));
    }

    @Test
    @DisplayName("toString - handles null values")
    void testToStringWithNullValues() {
        PdfResponse response = new PdfResponse(null, null);
        String toString = response.toString();

        assertNotNull(toString);
        assertTrue(toString.contains("PdfResponse"));
        assertTrue(toString.contains("null"));
    }

    @Test
    @DisplayName("toString - not null or empty")
    void testToStringNotNullOrEmpty() {
        PdfResponse response = new PdfResponse("base64", "file.pdf");
        String toString = response.toString();

        assertNotNull(toString);
        assertFalse(toString.isEmpty());
    }

    @Test
    @DisplayName("toString - with empty strings")
    void testToStringWithEmptyStrings() {
        PdfResponse response = new PdfResponse("", "");
        String toString = response.toString();

        assertNotNull(toString);
        assertTrue(toString.contains("PdfResponse"));
    }

    // ========== Edge Cases ==========

    @Test
    @DisplayName("Edge case - empty strings in constructor")
    void testEmptyStrings() {
        PdfResponse response = new PdfResponse("", "");
        assertEquals("", response.getBase64File());
        assertEquals("", response.getFileName());
    }

    @Test
    @DisplayName("Edge case - whitespace strings")
    void testWhitespaceStrings() {
        PdfResponse response = new PdfResponse("   ", "   ");
        assertEquals("   ", response.getBase64File());
        assertEquals("   ", response.getFileName());
    }

    @Test
    @DisplayName("Edge case - very long strings")
    void testVeryLongStrings() {
        String longBase64 = "A".repeat(10000);
        String longFileName = "B".repeat(1000);

        PdfResponse response = new PdfResponse(longBase64, longFileName);
        assertEquals(longBase64, response.getBase64File());
        assertEquals(longFileName, response.getFileName());
    }

    @Test
    @DisplayName("Edge case - special characters in fileName")
    void testSpecialCharactersInFileName() {
        PdfResponse response = new PdfResponse("base64", "file@#$%^&*().pdf");
        assertEquals("file@#$%^&*().pdf", response.getFileName());
    }

    @Test
    @DisplayName("Multiple setter invocations")
    void testMultipleSetterInvocations() {
        PdfResponse response = new PdfResponse("initial", "initial.pdf");

        response.setBase64File("update1");
        assertEquals("update1", response.getBase64File());

        response.setBase64File("update2");
        assertEquals("update2", response.getBase64File());

        response.setFileName("file1.pdf");
        assertEquals("file1.pdf", response.getFileName());

        response.setFileName("file2.pdf");
        assertEquals("file2.pdf", response.getFileName());
    }
}