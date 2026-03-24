package com.medgo.member.domain.entity.utilization;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.time.LocalDateTime;
import java.util.function.BiConsumer;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

class UtilizationLegacyEntityExtendedTest {



    @Test
    @DisplayName("All-args constructor populates all fields")
    void allArgsConstructor() {
        UtilizationLegacyId id = new UtilizationLegacyId("CTRL", LocalDateTime.of(2025,1,1,0,0));
        LocalDateTime now = LocalDateTime.of(2025,2,2,0,0);
        UtilizationLegacyEntity e = new UtilizationLegacyEntity(
                id,
                now, now.plusMinutes(5), "diag","dx","hosp","doc", 1.0,2.0,3.0,4.0,
                "mem","patient","company", now.plusDays(1), now.plusDays(2),
                "printed","bill",5.0,"reim","upd","valid","effective","soa","icdC","icdD","rem2","chk","pf","rcvd", now.plusHours(1),"depname","depcode"
        );
        assertAll(
                ()->assertEquals(id, e.getId()),
                ()->assertEquals("diag", e.getDiagDesc()),
                ()->assertEquals("dx", e.getDxRem()),
                ()->assertEquals("hosp", e.getHospitalName()),
                ()->assertEquals("doc", e.getDoctorName()),
                ()->assertEquals(1.0, e.getApproved()),
                ()->assertEquals(2.0, e.getDisapproved()),
                ()->assertEquals(3.0, e.getAdvances()),
                ()->assertEquals(4.0, e.getErc()),
                ()->assertEquals("mem", e.getMemcode()),
                ()->assertEquals("patient", e.getPatient()),
                ()->assertEquals("company", e.getCompany()),
                ()->assertEquals("printed", e.getPrintedBy()),
                ()->assertEquals("bill", e.getBillcode()),
                ()->assertEquals(5.0, e.getMedicareIncentives()),
                ()->assertEquals("reim", e.getReimReason()),
                ()->assertEquals("upd", e.getUpdatedBy()),
                ()->assertEquals("valid", e.getValid()),
                ()->assertEquals("effective", e.getEffective()),
                ()->assertEquals("soa", e.getHospSoa()),
                ()->assertEquals("icdC", e.getIcd10code()),
                ()->assertEquals("icdD", e.getIcd10desc()),
                ()->assertEquals("rem2", e.getRemarks2()),
                ()->assertEquals("chk", e.getChecknum()),
                ()->assertEquals("pf", e.getPf()),
                ()->assertEquals("rcvd", e.getRcvdBy()),
                ()->assertEquals("depname", e.getDepname()),
                ()->assertEquals("depcode", e.getDepcode())
        );
    }

    @Test
    @DisplayName("Constructor id difference breaks equality")
    void constructorIdDifference() {
        LocalDateTime t = LocalDateTime.of(2025,1,1,0,0);
        UtilizationLegacyEntity a = new UtilizationLegacyEntity(new UtilizationLegacyId("A", t), null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null);
        UtilizationLegacyEntity b = new UtilizationLegacyEntity(new UtilizationLegacyId("B", t), null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null);
        assertNotEquals(a,b);
    }

    @ParameterizedTest
    @MethodSource("lateDiffs")
    @DisplayName("equals late difference after matching earlier fields")
    void equalsLateDifference(BiConsumer<UtilizationLegacyEntity, UtilizationLegacyEntity> diff) {
        LocalDateTime now = LocalDateTime.of(2025,3,3,0,0);
        UtilizationLegacyEntity a = new UtilizationLegacyEntity(new UtilizationLegacyId("CTRL", now), now, now, "d","x","h","doc",1.0,2.0,3.0,4.0,
                "m","p","c", now, now, "print","bill",5.0,"reim","upd","valid","eff","soa","icdC","icdD","rem2","chk","pf","rcvd", now,"dep","code");
        UtilizationLegacyEntity b = new UtilizationLegacyEntity(new UtilizationLegacyId("CTRL", now), now, now, "d","x","h","doc",1.0,2.0,3.0,4.0,
                "m","p","c", now, now, "print","bill",5.0,"reim","upd","valid","eff","soa","icdC","icdD","rem2","chk","pf","rcvd", now,"dep","code");
        diff.accept(a,b);
        assertNotEquals(a,b);
        assertNotEquals(a.hashCode(), b.hashCode());
    }

    static Stream<BiConsumer<UtilizationLegacyEntity, UtilizationLegacyEntity>> lateDiffs() {
        return Stream.of(
                (a,b)-> b.setAvailFr(a.getAvailFr()!=null? a.getAvailFr().plusHours(1): LocalDateTime.of(2025,1,1,0,0)),
                (a,b)-> b.setAvailTo(a.getAvailTo()!=null? a.getAvailTo().plusHours(1): LocalDateTime.of(2025,1,1,1,0)),
                (a,b)-> b.setDiagDesc("diff"),(a,b)-> b.setDxRem("diff"),(a,b)-> b.setHospitalName("diff"),(a,b)-> b.setDoctorName("diff"),
                (a,b)-> b.setApproved(9.9),(a,b)-> b.setDisapproved(9.9),(a,b)-> b.setAdvances(9.9),(a,b)-> b.setErc(9.9),
                (a,b)-> b.setMemcode("diff"),(a,b)-> b.setPatient("diff"),(a,b)-> b.setCompany("diff"),
                (a,b)-> b.setPeriodFr(a.getPeriodFr()!=null? a.getPeriodFr().plusDays(1): LocalDateTime.now()),
                (a,b)-> b.setPeriodTo(a.getPeriodTo()!=null? a.getPeriodTo().plusDays(1): LocalDateTime.now()),
                (a,b)-> b.setPrintedBy("diff"),(a,b)-> b.setBillcode("diff"),(a,b)-> b.setMedicareIncentives(8.8),(a,b)-> b.setReimReason("diff"),
                (a,b)-> b.setUpdatedBy("diff"),(a,b)-> b.setValid("diff"),(a,b)-> b.setEffective("diff"),(a,b)-> b.setHospSoa("diff"),
                (a,b)-> b.setIcd10code("diff"),(a,b)-> b.setIcd10desc("diff"),(a,b)-> b.setRemarks2("diff"),(a,b)-> b.setChecknum("diff"),
                (a,b)-> b.setPf("diff"),(a,b)-> b.setRcvdBy("diff"),(a,b)-> b.setRcvdDate(LocalDateTime.of(2025,4,4,4,4)),
                (a,b)-> b.setDepname("diff"),(a,b)-> b.setDepcode("diff")
        );
    }

    @Test
    @DisplayName("All-null instances equal and hashCode match")
    void allNullInstancesEqual() {
        UtilizationLegacyEntity a = new UtilizationLegacyEntity();
        UtilizationLegacyEntity b = new UtilizationLegacyEntity();
        assertEquals(a,b);
        assertEquals(a.hashCode(), b.hashCode());
    }

    @Test
    @DisplayName("toString with null fields contains class name")
    void toStringNulls() {
        assertTrue(new UtilizationLegacyEntity().toString().contains("UtilizationLegacyEntity"));
    }
}

