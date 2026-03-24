package com.medgo.provider.mapper;

import com.medgo.provider.domain.entity.AffiliationView;
import com.medgo.provider.domain.response.ViewDoctorHospitalResponse;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class MedGoClaimsMapperTest {

    @Test
    void toViewDoctorHospitalResponse_mapsFields() {
        MedGoClaimsMapperImpl mapper = new MedGoClaimsMapperImpl();
        AffiliationView v = new AffiliationView();
        v.setLastName("Doe");
        v.setFirstName("John");
        v.setMiddleInitial("M");
        v.setDoctorCode("D1");
        v.setSpecializationCode("S1");
        v.setSpecializationDesc("Spec");
        v.setHospitalCode("H1");
        v.setHospitalName("Hosp");

        ViewDoctorHospitalResponse resp = mapper.toViewDoctorHospitalResponse(v);
        assertEquals("Doe", resp.getLastName());
        assertEquals("John", resp.getFirstName());
        assertEquals("D1", resp.getDoctorCode());
        assertNotNull(resp.getSpecializations());
        assertFalse(resp.getSpecializations().isEmpty());
        assertTrue(resp.getFullName().contains("Doe"));
    }
}























