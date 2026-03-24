package com.medgo.member.domain.request;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class UtilizationRequestTest {



    @Test
    @DisplayName("All-args constructor populates every field")
    void allArgsConstructor() {
        LocalDateTime fr = LocalDateTime.of(2025, 1, 1, 0, 0);
        LocalDateTime to = LocalDateTime.of(2025, 1, 2, 0, 0);
        LocalDateTime valDate = LocalDateTime.of(2025, 2, 1, 0, 0);
        LocalDateTime effDate = LocalDateTime.of(2025, 3, 1, 0, 0);
        UtilizationRequest req = new UtilizationRequest(fr, to, "MEM", "LN", "FN", "MI", "COMP", "USR", valDate, effDate, "last12months", 11, 44);

        assertAll(
                () -> assertEquals(fr, req.getDateFr()),
                () -> assertEquals(to, req.getDateTo()),
                () -> assertEquals("MEM", req.getMemcode()),
                () -> assertEquals("LN", req.getLname()),
                () -> assertEquals("FN", req.getFname()),
                () -> assertEquals("MI", req.getMi()),
                () -> assertEquals("COMP", req.getComp()),
                () -> assertEquals("USR", req.getUser()),
                () -> assertEquals(valDate, req.getValDate()),
                () -> assertEquals(effDate, req.getEffective()),
                () -> assertEquals("last12months", req.getPeriodType()),
                () -> assertEquals(11, req.getPage()),
                () -> assertEquals(44, req.getSize())
        );
    }

    @Test
    @DisplayName("No-args constructor creates empty object")
    void noArgsConstructor() {
        UtilizationRequest req = new UtilizationRequest();
        assertNotNull(req);
        assertNull(req.getMemcode());
        assertNull(req.getDateFr());
    }

    @Test
    @DisplayName("String setters parse both supported formats")
    void stringSetters() {
        UtilizationRequest req = new UtilizationRequest();
        req.setDateFr("2025-10-28");
        assertEquals(LocalDateTime.of(2025, 10, 28, 0, 0), req.getDateFr());
        req.setDateFr("2025-10-28T14:30:00");
        assertEquals(LocalDateTime.of(2025, 10, 28, 14, 30, 0), req.getDateFr());
        req.setDateTo("2025-10-29");
        assertEquals(LocalDateTime.of(2025, 10, 29, 0, 0), req.getDateTo());
        req.setDateTo("2025-10-29T06:05:04");
        assertEquals(LocalDateTime.of(2025, 10, 29, 6, 5, 4), req.getDateTo());
    }

    @Test
    @DisplayName("Invalid date string in setters causes exception via parse")
    void invalidStringSetterThrows() {
        UtilizationRequest req = new UtilizationRequest();
        assertThrows(Exception.class, () -> req.setDateFr("2025/10/28"));
        assertThrows(Exception.class, () -> req.setDateTo("28-10-2025"));
    }

    @Test
    @DisplayName("parseToLocalDateTime first pattern success (yyyy-MM-dd)")
    void parseFirstPattern() {
        UtilizationRequest req = new UtilizationRequest();
        LocalDateTime dt = req.parseToLocalDateTime("2025-12-25");
        assertEquals(LocalDateTime.of(2025, 12, 25, 0, 0), dt);
    }

    @Test
    @DisplayName("parseToLocalDateTime falls back to second pattern after exception")
    void parseSecondPattern() {
        UtilizationRequest req = new UtilizationRequest();
        LocalDateTime dt = req.parseToLocalDateTime("2025-12-25T23:59:58");
        assertEquals(LocalDateTime.of(2025, 12, 25, 23, 59, 58), dt);
    }

    @Test
    @DisplayName("parseToLocalDateTime invalid after both patterns throws")
    void parseInvalidThrows() {
        UtilizationRequest req = new UtilizationRequest();
        assertThrows(Exception.class, () -> req.parseToLocalDateTime("2025/12/25 23:59:58"));
    }

    // ==================== EQUALS METHOD - 100% COVERAGE ====================

    @Test
    @DisplayName("equals - same reference returns true")
    void equalsSameReference() {
        UtilizationRequest a = base();
        assertEquals(a, a);
    }

    @Test
    @DisplayName("equals - null returns false")
    void equalsNull() {
        UtilizationRequest a = base();
        assertNotEquals(null, a);
    }

    @Test
    @DisplayName("equals - different class returns false")
    void equalsDifferentClass() {
        UtilizationRequest a = base();
        assertNotEquals(a, "string");
        assertNotEquals(a, new Object());
    }

    @Test
    @DisplayName("equals - identical objects returns true")
    void equalsIdenticalObjects() {
        UtilizationRequest a = base();
        UtilizationRequest b = base();
        assertEquals(a, b);
        assertEquals(a.hashCode(), b.hashCode());
    }

    @Test
    @DisplayName("equals - dateFr null in this, not null in other")
    void equalsDateFrNullInThis() {
        UtilizationRequest a = base();
        a.setDateFr((LocalDateTime) null);
        UtilizationRequest b = base();
        assertNotEquals(a, b);
    }

    @Test
    @DisplayName("equals - dateFr not equal")
    void equalsDateFrNotEqual() {
        UtilizationRequest a = base();
        UtilizationRequest b = base();
        b.setDateFr(a.getDateFr().plusDays(1));
        assertNotEquals(a, b);
    }

    @Test
    @DisplayName("equals - dateTo null in this, not null in other")
    void equalsDateToNullInThis() {
        UtilizationRequest a = base();
        a.setDateTo((LocalDateTime) null);
        UtilizationRequest b = base();
        assertNotEquals(a, b);
    }

    @Test
    @DisplayName("equals - dateTo not equal")
    void equalsDateToNotEqual() {
        UtilizationRequest a = base();
        UtilizationRequest b = base();
        b.setDateTo(a.getDateTo().plusDays(1));
        assertNotEquals(a, b);
    }

    @Test
    @DisplayName("equals - memcode null in this, not null in other")
    void equalsMemcodeNullInThis() {
        UtilizationRequest a = base();
        a.setMemcode(null);
        UtilizationRequest b = base();
        assertNotEquals(a, b);
    }

    @Test
    @DisplayName("equals - memcode not equal")
    void equalsMemcodeNotEqual() {
        UtilizationRequest a = base();
        UtilizationRequest b = base();
        b.setMemcode("DIFFERENT");
        assertNotEquals(a, b);
    }

    @Test
    @DisplayName("equals - lname null in this, not null in other")
    void equalsLnameNullInThis() {
        UtilizationRequest a = base();
        a.setLname(null);
        UtilizationRequest b = base();
        assertNotEquals(a, b);
    }

    @Test
    @DisplayName("equals - lname not equal")
    void equalsLnameNotEqual() {
        UtilizationRequest a = base();
        UtilizationRequest b = base();
        b.setLname("DIFFERENT");
        assertNotEquals(a, b);
    }

    @Test
    @DisplayName("equals - fname null in this, not null in other")
    void equalsFnameNullInThis() {
        UtilizationRequest a = base();
        a.setFname(null);
        UtilizationRequest b = base();
        assertNotEquals(a, b);
    }

    @Test
    @DisplayName("equals - fname not equal")
    void equalsFnameNotEqual() {
        UtilizationRequest a = base();
        UtilizationRequest b = base();
        b.setFname("DIFFERENT");
        assertNotEquals(a, b);
    }

    @Test
    @DisplayName("equals - mi null in this, not null in other")
    void equalsMiNullInThis() {
        UtilizationRequest a = base();
        a.setMi(null);
        UtilizationRequest b = base();
        assertNotEquals(a, b);
    }

    @Test
    @DisplayName("equals - mi not equal")
    void equalsMiNotEqual() {
        UtilizationRequest a = base();
        UtilizationRequest b = base();
        b.setMi("X");
        assertNotEquals(a, b);
    }

    @Test
    @DisplayName("equals - comp null in this, not null in other")
    void equalsCompNullInThis() {
        UtilizationRequest a = base();
        a.setComp(null);
        UtilizationRequest b = base();
        assertNotEquals(a, b);
    }

    @Test
    @DisplayName("equals - comp not equal")
    void equalsCompNotEqual() {
        UtilizationRequest a = base();
        UtilizationRequest b = base();
        b.setComp("DIFFERENT");
        assertNotEquals(a, b);
    }

    @Test
    @DisplayName("equals - user null in this, not null in other")
    void equalsUserNullInThis() {
        UtilizationRequest a = base();
        a.setUser(null);
        UtilizationRequest b = base();
        assertNotEquals(a, b);
    }

    @Test
    @DisplayName("equals - user not equal")
    void equalsUserNotEqual() {
        UtilizationRequest a = base();
        UtilizationRequest b = base();
        b.setUser("DIFFERENT");
        assertNotEquals(a, b);
    }

    @Test
    @DisplayName("equals - valDate null in this, not null in other")
    void equalsValDateNullInThis() {
        UtilizationRequest a = base();
        a.setValDate(null);
        UtilizationRequest b = base();
        assertNotEquals(a, b);
    }

    @Test
    @DisplayName("equals - valDate not equal")
    void equalsValDateNotEqual() {
        UtilizationRequest a = base();
        UtilizationRequest b = base();
        b.setValDate(a.getValDate().plusHours(2));
        assertNotEquals(a, b);
    }

    @Test
    @DisplayName("equals - effective null in this, not null in other")
    void equalsEffectiveNullInThis() {
        UtilizationRequest a = base();
        a.setEffective(null);
        UtilizationRequest b = base();
        assertNotEquals(a, b);
    }

    @Test
    @DisplayName("equals - effective not equal")
    void equalsEffectiveNotEqual() {
        UtilizationRequest a = base();
        UtilizationRequest b = base();
        b.setEffective(a.getEffective().plusHours(5));
        assertNotEquals(a, b);
    }

    @Test
    @DisplayName("equals - periodType null in this, not null in other")
    void equalsPeriodTypeNullInThis() {
        UtilizationRequest a = base();
        a.setPeriodType(null);
        UtilizationRequest b = base();
        assertNotEquals(a, b);
    }

    @Test
    @DisplayName("equals - periodType not equal")
    void equalsPeriodTypeNotEqual() {
        UtilizationRequest a = base();
        UtilizationRequest b = base();
        b.setPeriodType("2years");
        assertNotEquals(a, b);
    }

    @Test
    @DisplayName("equals - page null in this, not null in other")
    void equalsPageNullInThis() {
        UtilizationRequest a = base();
        a.setPage(null);
        UtilizationRequest b = base();
        assertNotEquals(a, b);
    }

    @Test
    @DisplayName("equals - page not equal")
    void equalsPageNotEqual() {
        UtilizationRequest a = base();
        UtilizationRequest b = base();
        b.setPage(a.getPage() + 10);
        assertNotEquals(a, b);
    }

    @Test
    @DisplayName("equals - size null in this, not null in other")
    void equalsSizeNullInThis() {
        UtilizationRequest a = base();
        a.setSize(null);
        UtilizationRequest b = base();
        assertNotEquals(a, b);
    }

    @Test
    @DisplayName("equals - size not equal")
    void equalsSizeNotEqual() {
        UtilizationRequest a = base();
        UtilizationRequest b = base();
        b.setSize(a.getSize() + 20);
        assertNotEquals(a, b);
    }

    @Test
    @DisplayName("equals - both null for all fields")
    void equalsBothNullAllFields() {
        UtilizationRequest a = new UtilizationRequest();
        UtilizationRequest b = new UtilizationRequest();
        assertEquals(a, b);
        assertEquals(a.hashCode(), b.hashCode());
    }

    @Test
    @DisplayName("equals - both null for specific fields still equals")
    void equalsBothNullSpecificFields() {
        UtilizationRequest a = base();
        UtilizationRequest b = base();
        a.setLname(null);
        b.setLname(null);
        a.setFname(null);
        b.setFname(null);
        a.setMi(null);
        b.setMi(null);
        assertEquals(a, b);
    }

    @Test
    @DisplayName("equals - symmetric property")
    void equalsSymmetric() {
        UtilizationRequest a = base();
        UtilizationRequest b = base();
        assertTrue(a.equals(b) && b.equals(a));
    }

    @Test
    @DisplayName("equals - transitive property")
    void equalsTransitive() {
        UtilizationRequest a = base();
        UtilizationRequest b = base();
        UtilizationRequest c = base();
        assertTrue(a.equals(b) && b.equals(c) && a.equals(c));
    }

    @Test
    @DisplayName("equals - consistent across multiple invocations")
    void equalsConsistent() {
        UtilizationRequest a = base();
        UtilizationRequest b = base();
        for (int i = 0; i < 5; i++) {
            assertEquals(a, b);
        }
    }

    // ==================== HASHCODE METHOD ====================

    @Test
    @DisplayName("hashCode - identical objects same hash")
    void hashCodeIdentical() {
        UtilizationRequest a = base();
        UtilizationRequest b = base();
        assertEquals(a.hashCode(), b.hashCode());
    }

    @Test
    @DisplayName("hashCode - consistent across invocations")
    void hashCodeConsistent() {
        UtilizationRequest a = base();
        int h = a.hashCode();
        for (int i = 0; i < 5; i++) {
            assertEquals(h, a.hashCode());
        }
    }

    @Test
    @DisplayName("hashCode - changes when memcode changes")
    void hashCodeChangesMemcode() {
        UtilizationRequest a = base();
        UtilizationRequest b = base();
        b.setMemcode("DIFF");
        assertNotEquals(a.hashCode(), b.hashCode());
    }

    @Test
    @DisplayName("hashCode - changes when dateFr changes")
    void hashCodeChangesDateFr() {
        UtilizationRequest a = base();
        UtilizationRequest b = base();
        b.setDateFr(a.getDateFr().plusHours(1));
        assertNotEquals(a.hashCode(), b.hashCode());
    }

    @Test
    @DisplayName("hashCode - changes when dateTo changes")
    void hashCodeChangesDateTo() {
        UtilizationRequest a = base();
        UtilizationRequest b = base();
        b.setDateTo(a.getDateTo().plusHours(2));
        assertNotEquals(a.hashCode(), b.hashCode());
    }

    @Test
    @DisplayName("hashCode - changes when page changes")
    void hashCodeChangesPage() {
        UtilizationRequest a = base();
        UtilizationRequest b = base();
        b.setPage(99);
        assertNotEquals(a.hashCode(), b.hashCode());
    }

    @Test
    @DisplayName("hashCode - changes when size changes")
    void hashCodeChangesSize() {
        UtilizationRequest a = base();
        UtilizationRequest b = base();
        b.setSize(999);
        assertNotEquals(a.hashCode(), b.hashCode());
    }

    @Test
    @DisplayName("hashCode - same for both null fields")
    void hashCodeBothNull() {
        UtilizationRequest a = base();
        UtilizationRequest b = base();
        a.setPeriodType(null);
        b.setPeriodType(null);
        a.setPage(null);
        b.setPage(null);
        assertEquals(a.hashCode(), b.hashCode());
    }

    // ==================== TOSTRING METHOD ====================

    @Test
    @DisplayName("toString contains key fields")
    void toStringContains() {
        UtilizationRequest a = base();
        String s = a.toString();
        assertAll(
                () -> assertTrue(s.contains("memcode=MC")),
                () -> assertTrue(s.contains("lname=LN")),
                () -> assertTrue(s.contains("fname=FN")),
                () -> assertTrue(s.contains("periodType=custom")),
                () -> assertTrue(s.contains("page=1")),
                () -> assertTrue(s.contains("size=10"))
        );
    }

    @Test
    @DisplayName("toString with null fields")
    void toStringWithNulls() {
        UtilizationRequest req = new UtilizationRequest();
        String s = req.toString();
        assertNotNull(s);
        assertTrue(s.contains("UtilizationRequest"));
    }

    // ==================== GETTER/SETTER COVERAGE ====================

    @Test
    @DisplayName("All getters and setters work correctly")
    void gettersSetters() {
        UtilizationRequest req = new UtilizationRequest();

        LocalDateTime fr = LocalDateTime.of(2025, 6, 1, 10, 0);
        req.setDateFr(fr);
        assertEquals(fr, req.getDateFr());

        LocalDateTime to = LocalDateTime.of(2025, 6, 2, 10, 0);
        req.setDateTo(to);
        assertEquals(to, req.getDateTo());

        req.setMemcode("MEM123");
        assertEquals("MEM123", req.getMemcode());

        req.setLname("LastName");
        assertEquals("LastName", req.getLname());

        req.setFname("FirstName");
        assertEquals("FirstName", req.getFname());

        req.setMi("M");
        assertEquals("M", req.getMi());

        req.setComp("Company");
        assertEquals("Company", req.getComp());

        req.setUser("UserTest");
        assertEquals("UserTest", req.getUser());

        LocalDateTime val = LocalDateTime.of(2025, 6, 3, 10, 0);
        req.setValDate(val);
        assertEquals(val, req.getValDate());

        LocalDateTime eff = LocalDateTime.of(2025, 6, 4, 10, 0);
        req.setEffective(eff);
        assertEquals(eff, req.getEffective());

        req.setPeriodType("last6months");
        assertEquals("last6months", req.getPeriodType());

        req.setPage(5);
        assertEquals(5, req.getPage());

        req.setSize(50);
        assertEquals(50, req.getSize());
    }

    // ==================== HELPER METHOD ====================

    private UtilizationRequest base() {
        LocalDateTime fr = LocalDateTime.of(2025, 5, 10, 0, 0);
        LocalDateTime to = LocalDateTime.of(2025, 5, 11, 0, 0);
        LocalDateTime val = LocalDateTime.of(2025, 5, 12, 0, 0);
        LocalDateTime eff = LocalDateTime.of(2025, 5, 13, 0, 0);
        return new UtilizationRequest(fr, to, "MC", "LN", "FN", "MI", "COMP", "USR", val, eff, "custom", 1, 10);
    }
}
