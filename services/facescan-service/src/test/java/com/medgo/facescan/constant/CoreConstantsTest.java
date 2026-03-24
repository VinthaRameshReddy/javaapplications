package com.medgo.facescan.constant;

import com.medgo.facescan.types.PortalUserStatusEnum;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CoreConstantsTest {

    @Test
    void constants_have_expected_values() {
        // Verify constant strings are present and formatted as expected
        assertNotNull(CoreConstants.AUTH_INVALID_STATUS_FORMAT);
        assertTrue(CoreConstants.AUTH_INVALID_STATUS_FORMAT.contains("account"));

        // AUTH_LOCKED_STATUS_FORMAT should include the LOCKED status (uppercase)
        assertEquals(CoreConstants.AUTH_LOCKED_STATUS_FORMAT,
                String.format(CoreConstants.AUTH_INVALID_STATUS_FORMAT, PortalUserStatusEnum.LOCKED.toString().toUpperCase()));

        assertEquals("RESIGNED - EFF. ", CoreConstants.RESIGNED_EFF);
    }
}
