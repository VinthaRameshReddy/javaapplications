package com.medgo.member.domain.entity.utilization;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class UtilizationLegacyIdTest {



    @Test
    @DisplayName("equals and hashCode same values")
    void equalsSame() {
        LocalDateTime t = LocalDateTime.of(2025,1,1,0,0);
        UtilizationLegacyId a = new UtilizationLegacyId("CTRL", t);
        UtilizationLegacyId b = new UtilizationLegacyId("CTRL", t);
        assertEquals(a,b);
        assertEquals(a.hashCode(), b.hashCode());
    }

    @Test
    @DisplayName("equals differs by controlCode")
    void equalsDiffControlCode() {
        LocalDateTime t = LocalDateTime.now();
        UtilizationLegacyId a = new UtilizationLegacyId("A", t);
        UtilizationLegacyId b = new UtilizationLegacyId("B", t);
        assertNotEquals(a,b);
    }

    @Test
    @DisplayName("equals differs by updatedDate")
    void equalsDiffUpdatedDate() {
        LocalDateTime t = LocalDateTime.now();
        UtilizationLegacyId a = new UtilizationLegacyId("A", t);
        UtilizationLegacyId b = new UtilizationLegacyId("A", t.plusHours(1));
        assertNotEquals(a,b);
    }

    @Test
    @DisplayName("equals handles null fields")
    void equalsNullFields() {
        UtilizationLegacyId a = new UtilizationLegacyId(null, null);
        UtilizationLegacyId b = new UtilizationLegacyId(null, null);
        assertEquals(a,b);
        assertEquals(a.hashCode(), b.hashCode());
    }

    @Test
    @DisplayName("equals controlCode null vs value")
    void equalsControlCodeNullMismatch() {
        LocalDateTime t = LocalDateTime.now();
        UtilizationLegacyId a = new UtilizationLegacyId(null, t);
        UtilizationLegacyId b = new UtilizationLegacyId("X", t);
        assertNotEquals(a,b);
    }

    @Test
    @DisplayName("equals updatedDate null vs value")
    void equalsUpdatedDateNullMismatch() {
        UtilizationLegacyId a = new UtilizationLegacyId("X", null);
        UtilizationLegacyId b = new UtilizationLegacyId("X", LocalDateTime.now());
        assertNotEquals(a,b);
    }

    @Test
    @DisplayName("equals self, null and type")
    void equalsSelfNullType() {
        UtilizationLegacyId a = new UtilizationLegacyId("C", LocalDateTime.now());
        assertEquals(a,a);
        assertNotEquals(a,null);
        assertNotEquals(a,"string");
    }

    @Test
    @DisplayName("getControlCode/getUpdatedDate after setters")
    void gettersAfterSetters() {
        UtilizationLegacyId id = new UtilizationLegacyId();
        assertNull(id.getControlCode());
        assertNull(id.getUpdatedDate());
        LocalDateTime ts = LocalDateTime.of(2025,5,5,12,0);
        id.setControlCode("ABC123");
        id.setUpdatedDate(ts);
        assertEquals("ABC123", id.getControlCode());
        assertEquals(ts, id.getUpdatedDate());
    }

    @Test
    @DisplayName("setControlCode + setUpdatedDate overwrite previous values")
    void settersOverwriteValues() {
        LocalDateTime t1 = LocalDateTime.of(2025,1,1,0,0);
        LocalDateTime t2 = LocalDateTime.of(2025,2,2,0,0);
        UtilizationLegacyId id = new UtilizationLegacyId("OLD", t1);
        assertEquals("OLD", id.getControlCode());
        assertEquals(t1, id.getUpdatedDate());
        int oldHash = id.hashCode();
        id.setControlCode("NEW");
        id.setUpdatedDate(t2);
        assertEquals("NEW", id.getControlCode());
        assertEquals(t2, id.getUpdatedDate());
        assertNotEquals(oldHash, id.hashCode(), "hashCode should change after field modifications");
    }

    @Test
    @DisplayName("Set controlCode and updatedDate to null after having values")
    void settersSetToNull() {
        LocalDateTime t = LocalDateTime.of(2025,3,3,3,3);
        UtilizationLegacyId id = new UtilizationLegacyId("X", t);
        assertEquals("X", id.getControlCode());
        assertEquals(t, id.getUpdatedDate());
        id.setControlCode(null);
        id.setUpdatedDate(null);
        assertNull(id.getControlCode());
        assertNull(id.getUpdatedDate());
    }

    @Test
    @DisplayName("equals reflects setter changes")
    void equalsAfterSetterChanges() {
        LocalDateTime t = LocalDateTime.of(2025,4,4,4,4);
        UtilizationLegacyId a = new UtilizationLegacyId("A", t);
        UtilizationLegacyId b = new UtilizationLegacyId("A", t);
        assertEquals(a,b);
        b.setControlCode("B");
        assertNotEquals(a,b);
        b.setControlCode("A");
        b.setUpdatedDate(t.plusMinutes(1));
        assertNotEquals(a,b);
    }
}
