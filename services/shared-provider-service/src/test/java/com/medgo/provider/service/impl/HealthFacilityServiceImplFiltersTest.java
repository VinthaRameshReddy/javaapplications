package com.medgo.provider.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.medgo.commons.CommonResponse;
import com.medgo.provider.domain.entity.HealthFacilityEntity;
import com.medgo.provider.domain.request.HospitalRequest;
import com.medgo.provider.mapper.MedGoClaimsMapper;
import com.medgo.provider.repository.AffiliationViewRepository;
import com.medgo.provider.repository.HealthFacilityRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.mockito.stubbing.Answer;

import java.lang.reflect.Field;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class HealthFacilityServiceImplFiltersTest {

    private HealthFacilityServiceImpl service;

    @BeforeEach
    void setup() throws Exception {
        HealthFacilityRepository hfRepo = Mockito.mock(HealthFacilityRepository.class);
        AffiliationViewRepository affRepo = Mockito.mock(AffiliationViewRepository.class);
        MedGoClaimsMapper mapper = Mockito.mock(MedGoClaimsMapper.class);
        ObjectMapper om = Mockito.mock(ObjectMapper.class);
        Mockito.when(om.readValue(Mockito.any(java.io.InputStream.class), Mockito.any(TypeReference.class)))
                .thenAnswer((Answer<Object>) invocation -> {
                    // return two facilities: one with valid accountCodes json, one invalid
                    HealthFacilityEntity ok = new HealthFacilityEntity();
                    ok.setHfName("Alpha");
                    ok.setAccountCodes("[\"ACC1\",\"ACC2\"]");
                    HealthFacilityEntity bad = new HealthFacilityEntity();
                    bad.setHfName("Beta");
                    bad.setAccountCodes("not-json");
                    return List.of(ok, bad);
                });
        service = new HealthFacilityServiceImpl(hfRepo, affRepo, mapper, om);
        Field f = HealthFacilityServiceImpl.class.getDeclaredField("dbCallEnabled");
        f.setAccessible(true);
        f.set(service, false);
    }

    @Test
    void accountCodes_filter_matchesAndSkipsInvalidJson() {
        HospitalRequest req = new HospitalRequest();
        req.setAccountCodes(List.of("ACC1"));
        CommonResponse resp = service.getHospitalsList(0, 10, null, req);
        assertTrue(resp.isSuccess());
    }
}























