package com.medgo.provider.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.medgo.commons.CommonResponse;
import com.medgo.provider.domain.entity.AffiliationView;
import com.medgo.provider.domain.entity.HealthFacilityEntity;
import com.medgo.provider.domain.request.HospitalRequest;
import com.medgo.provider.domain.request.ViewDoctorHospitalRequest;
import com.medgo.provider.mapper.MedGoClaimsMapper;
import com.medgo.provider.repository.AffiliationViewRepository;
import com.medgo.provider.repository.HealthFacilityRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;

import java.lang.reflect.Field;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class HealthFacilityServiceImplDbSpecCoverageTest {

    private HealthFacilityRepository hfRepo;
    private AffiliationViewRepository affRepo;
    private MedGoClaimsMapper mapper;
    private HealthFacilityServiceImpl service;

    @BeforeEach
    void setup() throws Exception {
        hfRepo = Mockito.mock(HealthFacilityRepository.class);
        affRepo = Mockito.mock(AffiliationViewRepository.class);
        mapper = Mockito.mock(MedGoClaimsMapper.class);
        service = new HealthFacilityServiceImpl(hfRepo, affRepo, mapper, new ObjectMapper());
        Field f = HealthFacilityServiceImpl.class.getDeclaredField("dbCallEnabled");
        f.setAccessible(true);
        f.set(service, true);
    }

    @Test
    void getHospitalsList_buildsAllSpecs() {
        Mockito.when(hfRepo.findAll(ArgumentMatchers.<Specification<HealthFacilityEntity>>any(), ArgumentMatchers.any(org.springframework.data.domain.Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(), PageRequest.of(0, 5), 0));

        HospitalRequest req = new HospitalRequest();
        req.setHfCodes(List.of("H1"));
        req.setHfStatuses(List.of("ACTIVE"));
        req.setHfTypes(List.of("HOSPITAL"));
        req.setRegionCodes(List.of("R1"));
        req.setProvinceCodes(List.of("P1"));
        req.setCityCodes(List.of("C1"));
        req.setGroups(List.of("G1"));
        req.setAccountCodes(List.of("ACC1"));

        CommonResponse resp = service.getHospitalsList(0, 5, "name", req);
        assertFalse(resp.isSuccess()); // empty result expected
    }

    @Test
    void getViewDoctorHospitalV2_buildsAllSpecs() {
        Mockito.when(affRepo.findAll(ArgumentMatchers.<Specification<AffiliationView>>any(), ArgumentMatchers.any(org.springframework.data.domain.Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(), PageRequest.of(0, 5), 0));

        ViewDoctorHospitalRequest req = new ViewDoctorHospitalRequest();
        req.setHospitalCode("H1");
        req.setDoctorCode("D1");
        req.setDoctorName("Doc");
        req.setHospitalName("Hosp");
        req.setCityCode("C1");
        req.setProvinceCode("P1");
        req.setRegionCode("R1");
        req.setAccreditationStatusCodes(List.of("ACTIVE"));
        req.setSpecializationCodes(List.of("S1"));
        req.setSpecializationCodeNotIn(List.of("S2"));
        req.setMaternity(true);

        CommonResponse resp = service.getViewDoctorHospitalV2(0, 5, req);
        assertFalse(resp.isSuccess()); // empty result expected
    }
}


