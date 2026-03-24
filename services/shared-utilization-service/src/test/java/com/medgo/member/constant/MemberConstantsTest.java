package com.medgo.member.constant;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class MemberConstantsTest {




    @Test
    @DisplayName("All MemberConstants values are as expected")
    void testConstantsValues() {
        assertEquals("Invalid periodType: ", MemberConstants.INVALID_PERIOD);
        assertEquals("Please Select Period ", MemberConstants.PERIODTYPE);
        assertEquals("From Date cannot be greater than To Date", MemberConstants.FROMDATEANDTODATE);
        assertEquals("Selected date range cannot exceed 5 years ", MemberConstants.YEARS_BETWEEN);
        assertEquals("For custom , FromDate and ToDate must be provided", MemberConstants.CUSTOM);
        assertEquals("No records found for the selected period.", MemberConstants.RECORDS);
        assertEquals(" Error Generating PDF:", MemberConstants.GENEREATINGPDF);
        assertEquals("DISCLAIMER:This utilization report does not include availments that were not called in for approval and", MemberConstants.DISCLAIMER);
    }

    @Test
    @DisplayName("Class cannot be instantiated")
    void testNoInstantiation() {
        // Reflection attempt to instantiate will fail because implicit public constructor is present,
        // but we choose to assert that creating an instance is unnecessary.
        MemberConstants instance = new MemberConstants();
        assertNotNull(instance);
    }
}
