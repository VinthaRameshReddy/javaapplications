package com.medgo.member.domain.entity.utilization;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class UtilizationLegacyEntityTest {

    @Test
    void testGettersSettersEqualsHashCodeToString() {

        LocalDateTime now = LocalDateTime.now();

        UtilizationLegacyId id1 = new UtilizationLegacyId();
        UtilizationLegacyId id2 = new UtilizationLegacyId();

        UtilizationLegacyEntity e1 = new UtilizationLegacyEntity();
        UtilizationLegacyEntity e2 = new UtilizationLegacyEntity();

        // Set values on e1
        e1.setId(id1);
        e1.setAvailFr(now);
        e1.setAvailTo(now);
        e1.setDiagDesc("diagDesc");
        e1.setDxRem("dxRem");
        e1.setHospitalName("hospitalName");
        e1.setDoctorName("doctorName");
        e1.setApproved(123.45);
        e1.setDisapproved(67.89);
        e1.setAdvances(10.0);
        e1.setErc(5.5);
        e1.setMemcode("memcode");
        e1.setPatient("patient");
        e1.setCompany("company");
        e1.setPeriodFr(now);
        e1.setPeriodTo(now);
        e1.setPrintedBy("printedBy");
        e1.setBillcode("billcode");
        e1.setMedicareIncentives(15.0);
        e1.setReimReason("reimReason");
        e1.setUpdatedBy("updatedBy");
        e1.setValid("valid");
        e1.setEffective("effective");
        e1.setHospSoa("hospSoa");
        e1.setIcd10code("icd10code");
        e1.setIcd10desc("icd10desc");
        e1.setRemarks2("remarks2");
        e1.setChecknum("checknum");
        e1.setPf("pf");
        e1.setRcvdBy("rcvdBy");
        e1.setRcvdDate(now);
        e1.setDepname("depname");
        e1.setDepcode("depcode");

        // Set same values on e2 for equals() to match
        e2.setId(id1);
        e2.setAvailFr(now);
        e2.setAvailTo(now);
        e2.setDiagDesc("diagDesc");
        e2.setDxRem("dxRem");
        e2.setHospitalName("hospitalName");
        e2.setDoctorName("doctorName");
        e2.setApproved(123.45);
        e2.setDisapproved(67.89);
        e2.setAdvances(10.0);
        e2.setErc(5.5);
        e2.setMemcode("memcode");
        e2.setPatient("patient");
        e2.setCompany("company");
        e2.setPeriodFr(now);
        e2.setPeriodTo(now);
        e2.setPrintedBy("printedBy");
        e2.setBillcode("billcode");
        e2.setMedicareIncentives(15.0);
        e2.setReimReason("reimReason");
        e2.setUpdatedBy("updatedBy");
        e2.setValid("valid");
        e2.setEffective("effective");
        e2.setHospSoa("hospSoa");
        e2.setIcd10code("icd10code");
        e2.setIcd10desc("icd10desc");
        e2.setRemarks2("remarks2");
        e2.setChecknum("checknum");
        e2.setPf("pf");
        e2.setRcvdBy("rcvdBy");
        e2.setRcvdDate(now);
        e2.setDepname("depname");
        e2.setDepcode("depcode");

        // Self equals
        assertEquals(e1, e1);



        // Equals
        assertEquals(e1, e2);
        assertEquals(e1.hashCode(), e2.hashCode());

//        // Not equals
//        e2.setId(id2);
//        assertNotEquals(e1.hashCode(), e2.hashCode());

        // Not equals with null
        assertNotEquals(e1, null);

        // Not equals with different type
        assertNotEquals(e1, "string");

        // toString() should not be null or empty
        assertNotNull(e1.toString());
        assertFalse(e1.toString().isEmpty());
    }

    @Test
    void testSetIdNoOpAndFieldDifferences() {
        UtilizationLegacyEntity e = new UtilizationLegacyEntity();
        UtilizationLegacyId id = new UtilizationLegacyId("CTRL", LocalDateTime.now());
        e.setId(id); // no-op
        assertNull(e.getId(), "Custom setId is a no-op; id should remain null");
        e.setId(null);
        assertNull(e.getId(), "Still null after setting null");
    }

    @Test
    void testEqualsDiffersByEachField() {
        LocalDateTime now = LocalDateTime.now();
        UtilizationLegacyEntity base = build(now);
        // id difference cannot be created due to no-op setId; use other fields
        UtilizationLegacyEntity diff;

        diff = clone(base); diff.setAvailFr(now.minusDays(1)); assertNotEquals(base, diff);
        diff = clone(base); diff.setAvailTo(now.minusDays(2)); assertNotEquals(base, diff);
        diff = clone(base); diff.setDiagDesc("other"); assertNotEquals(base, diff);
        diff = clone(base); diff.setDxRem("other"); assertNotEquals(base, diff);
        diff = clone(base); diff.setHospitalName("other"); assertNotEquals(base, diff);
        diff = clone(base); diff.setDoctorName("other"); assertNotEquals(base, diff);
        diff = clone(base); diff.setApproved(999.0); assertNotEquals(base, diff);
        diff = clone(base); diff.setDisapproved(999.0); assertNotEquals(base, diff);
        diff = clone(base); diff.setAdvances(999.0); assertNotEquals(base, diff);
        diff = clone(base); diff.setErc(999.0); assertNotEquals(base, diff);
        diff = clone(base); diff.setMemcode("other"); assertNotEquals(base, diff);
        diff = clone(base); diff.setPatient("other"); assertNotEquals(base, diff);
        diff = clone(base); diff.setCompany("other"); assertNotEquals(base, diff);
        diff = clone(base); diff.setPeriodFr(now.minusDays(10)); assertNotEquals(base, diff);
        diff = clone(base); diff.setPeriodTo(now.plusDays(10)); assertNotEquals(base, diff);
        diff = clone(base); diff.setPrintedBy("x"); assertNotEquals(base, diff);
        diff = clone(base); diff.setBillcode("x"); assertNotEquals(base, diff);
        diff = clone(base); diff.setMedicareIncentives(777.0); assertNotEquals(base, diff);
        diff = clone(base); diff.setReimReason("x"); assertNotEquals(base, diff);
        diff = clone(base); diff.setUpdatedBy("x"); assertNotEquals(base, diff);
        diff = clone(base); diff.setValid("x"); assertNotEquals(base, diff);
        diff = clone(base); diff.setEffective("x"); assertNotEquals(base, diff);
        diff = clone(base); diff.setHospSoa("x"); assertNotEquals(base, diff);
        diff = clone(base); diff.setIcd10code("x"); assertNotEquals(base, diff);
        diff = clone(base); diff.setIcd10desc("x"); assertNotEquals(base, diff);
        diff = clone(base); diff.setRemarks2("x"); assertNotEquals(base, diff);
        diff = clone(base); diff.setChecknum("x"); assertNotEquals(base, diff);
        diff = clone(base); diff.setPf("x"); assertNotEquals(base, diff);
        diff = clone(base); diff.setRcvdBy("x"); assertNotEquals(base, diff);
        diff = clone(base); diff.setRcvdDate(now.minusYears(1)); assertNotEquals(base, diff);
        diff = clone(base); diff.setDepname("x"); assertNotEquals(base, diff);
        diff = clone(base); diff.setDepcode("x"); assertNotEquals(base, diff);
    }

    @Test
    void testEqualsHandlesNulls() {
        UtilizationLegacyEntity a = new UtilizationLegacyEntity();
        UtilizationLegacyEntity b = new UtilizationLegacyEntity();
        assertEquals(a, b, "Both empty entities should be equal");
        b.setCompany("c");
        assertNotEquals(a, b);
        a.setCompany("c");
        assertEquals(a, b);
    }

    private UtilizationLegacyEntity build(LocalDateTime now) {
        UtilizationLegacyEntity e = new UtilizationLegacyEntity();
        e.setAvailFr(now);
        e.setAvailTo(now);
        e.setDiagDesc("diagDesc");
        e.setDxRem("dxRem");
        e.setHospitalName("hospitalName");
        e.setDoctorName("doctorName");
        e.setApproved(123.45);
        e.setDisapproved(67.89);
        e.setAdvances(10.0);
        e.setErc(5.5);
        e.setMemcode("memcode");
        e.setPatient("patient");
        e.setCompany("company");
        e.setPeriodFr(now);
        e.setPeriodTo(now);
        e.setPrintedBy("printedBy");
        e.setBillcode("billcode");
        e.setMedicareIncentives(15.0);
        e.setReimReason("reimReason");
        e.setUpdatedBy("updatedBy");
        e.setValid("valid");
        e.setEffective("effective");
        e.setHospSoa("hospSoa");
        e.setIcd10code("icd10code");
        e.setIcd10desc("icd10desc");
        e.setRemarks2("remarks2");
        e.setChecknum("checknum");
        e.setPf("pf");
        e.setRcvdBy("rcvdBy");
        e.setRcvdDate(now);
        e.setDepname("depname");
        e.setDepcode("depcode");
        return e;
    }

    private UtilizationLegacyEntity clone(UtilizationLegacyEntity src) {
        UtilizationLegacyEntity c = new UtilizationLegacyEntity();
        c.setAvailFr(src.getAvailFr());
        c.setAvailTo(src.getAvailTo());
        c.setDiagDesc(src.getDiagDesc());
        c.setDxRem(src.getDxRem());
        c.setHospitalName(src.getHospitalName());
        c.setDoctorName(src.getDoctorName());
        c.setApproved(src.getApproved());
        c.setDisapproved(src.getDisapproved());
        c.setAdvances(src.getAdvances());
        c.setErc(src.getErc());
        c.setMemcode(src.getMemcode());
        c.setPatient(src.getPatient());
        c.setCompany(src.getCompany());
        c.setPeriodFr(src.getPeriodFr());
        c.setPeriodTo(src.getPeriodTo());
        c.setPrintedBy(src.getPrintedBy());
        c.setBillcode(src.getBillcode());
        c.setMedicareIncentives(src.getMedicareIncentives());
        c.setReimReason(src.getReimReason());
        c.setUpdatedBy(src.getUpdatedBy());
        c.setValid(src.getValid());
        c.setEffective(src.getEffective());
        c.setHospSoa(src.getHospSoa());
        c.setIcd10code(src.getIcd10code());
        c.setIcd10desc(src.getIcd10desc());
        c.setRemarks2(src.getRemarks2());
        c.setChecknum(src.getChecknum());
        c.setPf(src.getPf());
        c.setRcvdBy(src.getRcvdBy());
        c.setRcvdDate(src.getRcvdDate());
        c.setDepname(src.getDepname());
        c.setDepcode(src.getDepcode());
        return c;
    }
}
