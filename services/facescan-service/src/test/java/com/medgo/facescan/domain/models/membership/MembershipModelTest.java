package com.medgo.facescan.domain.models.membership;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class MembershipModelTest {

    @Test
    void lombok_getters_and_setters_and_constructors_work() {
        MembershipModel m = new MembershipModel();
        m.setMemberCode("M001");
        m.setFirstName("John");
        m.setLastName("Doe");
        LocalDateTime now = LocalDateTime.of(1990,1,1,0,0);
        m.setBirthDate(now);

        assertEquals("M001", m.getMemberCode());
        assertEquals("John", m.getFirstName());
        assertEquals("Doe", m.getLastName());
        assertEquals(now, m.getBirthDate());

        // exercise additional setters to increase coverage
        m.setAccountCode("AC001");
        m.setAccountName("AC_NAME");
        m.setAccountType("AC_TYPE");
        m.setMemStatus("ACTIVE");
        m.setPlanDesc("Plan X");

        assertEquals("AC001", m.getAccountCode());
        assertEquals("AC_NAME", m.getAccountName());
        assertEquals("AC_TYPE", m.getAccountType());
        assertEquals("ACTIVE", m.getMemStatus());
        assertEquals("Plan X", m.getPlanDesc());

        // exercise the custom (no-op) constructors to increase coverage
        new MembershipModel("M003", "dobString");
        new MembershipModel("M004", LocalDateTime.now());
    }
}
