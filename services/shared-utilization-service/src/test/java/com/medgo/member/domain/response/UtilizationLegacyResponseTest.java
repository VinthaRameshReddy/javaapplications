package com.medgo.member.domain.response;

import org.junit.jupiter.api.Test;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;




/**
 * JUnit 5 test case for UtilizationLegacyResponse.
 * Achieves 100% code coverage by testing all Lombok-generated methods.
 */
class UtilizationLegacyResponseTest {

    // Helper data for testing
    // Must initialize UtilizationResponse using no-args constructor and setters,
    // as the underlying DTO no longer has an AllArgsConstructor.
    private final UtilizationResponse response1 = createUtilizationResponse("ID001", "Service A");
    private final UtilizationResponse response2 = createUtilizationResponse("ID002", "Service B");
    private final List<UtilizationResponse> mockList = Arrays.asList(response1, response2);
    
    // Helper method for initialization
    private UtilizationResponse createUtilizationResponse(String code, String desc) {
        UtilizationResponse response = new UtilizationResponse();
        response.setControlCode(code);
        response.setDiagDesc(desc);
        return response;
    }


    /**
     * Test case for the all-arguments constructor and getters.
     * This covers the @AllArgsConstructor and all @Data-generated getters.
     */
    @Test
    void testAllArgsConstructorAndGetters() {
        // Arrange
        int expectedStatus = 200;
        String expectedMessage = "Success";

        // Act
        UtilizationLegacyResponse response = new UtilizationLegacyResponse(
                expectedStatus,
                expectedMessage,
                mockList
        );

        // Assert
        assertEquals(expectedStatus, response.getStatus(), "Status should match the value passed to constructor.");
        assertEquals(expectedMessage, response.getMessage(), "Message should match the value passed to constructor.");
        assertEquals(mockList, response.getList(), "List should match the value passed to constructor.");
        assertEquals(2, response.getList().size(), "List size should be correct.");
    }

    /**
     * Test case for the no-arguments constructor and setters.
     * This covers the @NoArgsConstructor and all @Data-generated setters and getters.
     */
    @Test
    void testNoArgsConstructorAndSetters() {
        // Arrange
        int newStatus = 404;
        String newMessage = "Not Found";
        List<UtilizationResponse> emptyList = Collections.emptyList();

        // Act
        UtilizationLegacyResponse response = new UtilizationLegacyResponse(); // Covers @NoArgsConstructor

        response.setStatus(newStatus);       // Covers status setter
        response.setMessage(newMessage);     // Covers message setter
        response.setList(emptyList);         // Covers list setter

        // Assert
        assertEquals(newStatus, response.getStatus(), "Status getter/setter failed.");
        assertEquals(newMessage, response.getMessage(), "Message getter/setter failed.");
        assertEquals(emptyList, response.getList(), "List getter/setter failed.");
        assertTrue(response.getList().isEmpty(), "List should be empty.");
    }

    /**
     * Test case for the generated toString() method.
     * Ensures the method executes and includes field names/values.
     */
    @Test
    void testToString() {
        // Arrange
        UtilizationLegacyResponse response = new UtilizationLegacyResponse(
                200, "OK", mockList
        );

        // Act
        String result = response.toString();

        // Assert
        assertNotNull(result, "toString() should not be null.");
        assertTrue(result.contains("status=200"), "toString() should contain status field.");
        assertTrue(result.contains("message=OK"), "toString() should contain message field.");
        assertTrue(result.contains("list="), "toString() should contain list field.");
    }

    /**
     * Test case for the generated equals() and hashCode() methods.
     * Ensures two identical objects are equal and have the same hash code.
     */
    @Test
    void testEqualsAndHashCode() {
        // Arrange
        UtilizationLegacyResponse responseA = new UtilizationLegacyResponse(100, "Test", mockList);
        UtilizationLegacyResponse responseB = new UtilizationLegacyResponse(100, "Test", mockList);
        
        // Need to create a new list for responseD so it doesn't share the same list reference as A and B if we were to change its content
        UtilizationResponse diffResponse = createUtilizationResponse("ID999", "Different Service");
        List<UtilizationResponse> diffList = Collections.singletonList(diffResponse);
        
        UtilizationLegacyResponse responseC = new UtilizationLegacyResponse(500, "Error", mockList); // Different status
        UtilizationLegacyResponse responseD = new UtilizationLegacyResponse(100, "Test", diffList); // Different list

        // Assert
        // Reflexivity
        assertEquals(responseA, responseA, "Object should be equal to itself (reflexivity).");
        
        // Equality
        assertEquals(responseA, responseB, "Identical objects should be equal.");
        assertEquals(responseA.hashCode(), responseB.hashCode(), "Identical objects must have equal hash codes.");

        // Inequality (Different status)
        assertNotEquals(responseA, responseC, "Objects with different status should not be equal.");

        // Inequality (Different list)
        assertNotEquals(responseA, responseD, "Objects with different list content should not be equal.");

        // Inequality (Null and other type)
        assertNotEquals(null, responseA, "Object should not be equal to null.");
        assertNotEquals(responseA, new Object(), "Object should not be equal to a different class instance.");
    }
}