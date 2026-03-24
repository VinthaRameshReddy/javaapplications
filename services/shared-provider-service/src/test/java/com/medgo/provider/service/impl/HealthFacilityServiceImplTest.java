package com.medgo.provider.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.medgo.commons.CommonResponse;
import com.medgo.provider.domain.entity.AffiliationView;
import com.medgo.provider.domain.entity.HealthFacilityEntity;
import com.medgo.provider.domain.request.HospitalRequest;
import com.medgo.provider.domain.request.ViewDoctorHospitalRequest;
import com.medgo.provider.domain.response.ViewDoctorHospitalResponse;
import com.medgo.provider.mapper.MedGoClaimsMapper;
import com.medgo.provider.repository.AffiliationViewRepository;
import com.medgo.provider.repository.HealthFacilityRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;

import java.lang.reflect.Field;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class HealthFacilityServiceImplTest {

    private HealthFacilityRepository healthFacilityRepository;
    private AffiliationViewRepository affiliationViewRepository;
    private MedGoClaimsMapper mapper;
    private ObjectMapper objectMapper;
    private HealthFacilityServiceImpl service;

    @BeforeEach
    void setUp() {
        healthFacilityRepository = Mockito.mock(HealthFacilityRepository.class);
        affiliationViewRepository = Mockito.mock(AffiliationViewRepository.class);
        mapper = Mockito.mock(MedGoClaimsMapper.class);
        objectMapper = new ObjectMapper();
        service = new HealthFacilityServiceImpl(healthFacilityRepository, affiliationViewRepository, mapper, objectMapper);
    }

    private void setDbCallEnabled(boolean value) throws Exception {
        Field f = HealthFacilityServiceImpl.class.getDeclaredField("dbCallEnabled");
        f.setAccessible(true);
        f.set(service, value);
    }

    @Test
    void getHospitalsList_dbModeEmpty_returnsNotFound() throws Exception {
        setDbCallEnabled(true);
        when(healthFacilityRepository.findAll(ArgumentMatchers.<Specification<HealthFacilityEntity>>any(), ArgumentMatchers.any(org.springframework.data.domain.Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(), PageRequest.of(0, 5), 0));

        CommonResponse resp = service.getHospitalsList(0, 5, null, new HospitalRequest());
        assertFalse(resp.isSuccess());
    }

    @Test
    void getHospitalsList_dbModeNonEmpty_returnsSuccess() throws Exception {
        setDbCallEnabled(true);
        HealthFacilityEntity e = new HealthFacilityEntity();
        e.setHfName("Test");
        when(healthFacilityRepository.findAll(ArgumentMatchers.<Specification<HealthFacilityEntity>>any(), ArgumentMatchers.any(org.springframework.data.domain.Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(e), PageRequest.of(0, 5), 1));

        CommonResponse resp = service.getHospitalsList(0, 5, "te", new HospitalRequest());
        assertTrue(resp.isSuccess());
    }

    @Test
    void getViewDoctorHospitalV2_dbModeEmpty_returnsNotFound() throws Exception {
        setDbCallEnabled(true);
        when(affiliationViewRepository.findAll(ArgumentMatchers.<Specification<AffiliationView>>any(), ArgumentMatchers.any(org.springframework.data.domain.Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(), PageRequest.of(0, 5), 0));

        CommonResponse resp = service.getViewDoctorHospitalV2(0, 5, new ViewDoctorHospitalRequest());
        assertFalse(resp.isSuccess());
    }

    @Test
    void getViewDoctorHospitalV2_dbModeNonEmpty_returnsSuccess() throws Exception {
        setDbCallEnabled(true);
        AffiliationView view = new AffiliationView();
        when(affiliationViewRepository.findAll(ArgumentMatchers.<Specification<AffiliationView>>any(), ArgumentMatchers.any(org.springframework.data.domain.Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(view), PageRequest.of(0, 5), 1));
        when(mapper.toViewDoctorHospitalResponse(any())).thenReturn(new ViewDoctorHospitalResponse());

        CommonResponse resp = service.getViewDoctorHospitalV2(0, 5, new ViewDoctorHospitalRequest());
        assertTrue(resp.isSuccess());
    }
}


