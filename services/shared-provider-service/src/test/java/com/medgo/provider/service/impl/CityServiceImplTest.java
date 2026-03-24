package com.medgo.provider.service.impl;

import com.medgo.commons.CommonResponse;
import com.medgo.provider.domain.entity.CityEntity;
import com.medgo.provider.domain.request.CityRequest;
import com.medgo.provider.repository.CityRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class CityServiceImplTest {

    private CityRepository cityRepository;
    private CityServiceImpl service;

    @BeforeEach
    void setUp() {
        cityRepository = Mockito.mock(CityRepository.class);
        service = new CityServiceImpl(cityRepository);
    }

    @Test
    void getCities_whenNoSearch_returnsActivePage() {
        CityEntity c1 = new CityEntity();
        c1.setProvinceId(1L);
        c1.setCode("C1");
        CityEntity c2 = new CityEntity();
        c2.setProvinceId(2L);
        c2.setCode("C2");
        Page<CityEntity> page = new PageImpl<>(List.of(c1, c2), PageRequest.of(0, 10), 2);
        when(cityRepository.findByDeleted(eq("N"), ArgumentMatchers.any())).thenReturn(page);

        CommonResponse resp = service.getCities(0, 10, null, null);

        assertTrue(resp.isSuccess());
    }

    @Test
    void getCities_whenSearchProvided_callsSearchRepository() {
        CityEntity c1 = new CityEntity();
        c1.setProvinceId(1L);
        c1.setCode("AA");
        Page<CityEntity> page = new PageImpl<>(List.of(c1), PageRequest.of(0, 10), 1);
        when(cityRepository.searchActive(eq("N"), eq("aa"), ArgumentMatchers.any())).thenReturn(page);

        CommonResponse resp = service.getCities(0, 10, " aa ", null);
        assertTrue(resp.isSuccess());
    }

    @Test
    void getCities_filtersByProvinceIdsAndCodes() {
        CityEntity c1 = new CityEntity();
        c1.setProvinceId(1L);
        c1.setCode("C1");
        CityEntity c2 = new CityEntity();
        c2.setProvinceId(2L);
        c2.setCode("C2");
        Page<CityEntity> page = new PageImpl<>(List.of(c1, c2), PageRequest.of(0, 10), 2);
        when(cityRepository.findByDeleted(eq("N"), ArgumentMatchers.any())).thenReturn(page);

        CityRequest req = new CityRequest();
        req.setProvinceIds(List.of(1L));
        req.setCodes(List.of("C1"));

        CommonResponse resp = service.getCities(0, 10, null, req);
        assertTrue(resp.isSuccess());
    }

    @Test
    void getCities_whenEmptyResult_returnsNotFound() {
        Page<CityEntity> page = new PageImpl<>(List.of(), PageRequest.of(0, 10), 0);
        when(cityRepository.findByDeleted(eq("N"), ArgumentMatchers.any())).thenReturn(page);

        CommonResponse resp = service.getCities(0, 10, null, null);
        assertFalse(resp.isSuccess());
    }
}

