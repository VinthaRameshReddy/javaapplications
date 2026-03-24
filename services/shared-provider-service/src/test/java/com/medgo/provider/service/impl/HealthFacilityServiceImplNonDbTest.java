package com.medgo.provider.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.medgo.commons.CommonResponse;
import com.medgo.provider.domain.request.HospitalRequest;
import com.medgo.provider.domain.request.ViewDoctorHospitalRequest;
import com.medgo.provider.mapper.MedGoClaimsMapper;
import com.medgo.provider.repository.AffiliationViewRepository;
import com.medgo.provider.repository.HealthFacilityRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.mockito.stubbing.Answer;

import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.assertTrue;

class HealthFacilityServiceImplNonDbTest {

    private HealthFacilityServiceImpl service;

    @BeforeEach
    void setUp() throws Exception {
        HealthFacilityRepository hfRepo = Mockito.mock(HealthFacilityRepository.class);
        AffiliationViewRepository affRepo = Mockito.mock(AffiliationViewRepository.class);
        MedGoClaimsMapper mapper = Mockito.mock(MedGoClaimsMapper.class);
        ObjectMapper om = Mockito.mock(ObjectMapper.class);
        // Stub JSON loads
        Mockito.when(om.readValue(Mockito.any(java.io.InputStream.class), Mockito.any(TypeReference.class)))
                .thenAnswer((Answer<Object>) invocation -> {
                    TypeReference<?> tr = invocation.getArgument(1);
                    String typeName = tr.getType().getTypeName();
                    if (typeName.contains("HealthFacilityEntity")) {
                        var e = new com.medgo.provider.domain.entity.HealthFacilityEntity();
                        e.setHfName("Alpha Hospital");
                        e.setAccountCodes("[\"ACC1\",\"ACC2\"]");
                        return java.util.List.of(e);
                    } else {
                        return java.util.List.of(new com.medgo.provider.domain.response.ViewDoctorHospitalResponse());
                    }
                });
        service = new HealthFacilityServiceImpl(hfRepo, affRepo, mapper, om);
        Field f = HealthFacilityServiceImpl.class.getDeclaredField("dbCallEnabled");
        f.setAccessible(true);
        f.set(service, false); // force JSON path
    }

    @Test
    void getHospitalsList_jsonPath_success() {
        CommonResponse resp = service.getHospitalsList(0, 3, null, new HospitalRequest());
        assertTrue(resp.isSuccess());
    }

    @Test
    void getViewDoctorHospitalV2_jsonPath_success() {
        CommonResponse resp = service.getViewDoctorHospitalV2(0, 3, new ViewDoctorHospitalRequest());
        assertTrue(resp.isSuccess());
    }
}


