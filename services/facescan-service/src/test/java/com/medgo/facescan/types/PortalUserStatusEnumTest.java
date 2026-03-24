package com.medgo.facescan.types;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PortalUserStatusEnumTest {

    @Test
    void enum_contains_expected_values() {
        PortalUserStatusEnum[] vals = PortalUserStatusEnum.values();
        assertNotNull(vals);
        // ensure common values exist
        assertTrue(java.util.Arrays.asList(vals).contains(PortalUserStatusEnum.ACTIVE));
        assertTrue(java.util.Arrays.asList(vals).contains(PortalUserStatusEnum.LOCKED));
        assertEquals(6, vals.length);
    }
}

