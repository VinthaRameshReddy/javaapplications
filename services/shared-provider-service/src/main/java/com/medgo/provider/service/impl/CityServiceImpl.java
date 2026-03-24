package com.medgo.provider.service.impl;

import com.medgo.commons.CommonResponse;
import com.medgo.commons.ErrorResponse;
import com.medgo.provider.domain.entity.CityEntity;
import com.medgo.provider.domain.request.CityRequest;
import com.medgo.provider.repository.CityRepository;
import com.medgo.provider.service.CityService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CityServiceImpl implements CityService {

    private static final Logger LOGGER = LoggerFactory.getLogger(CityServiceImpl.class);
    private final CityRepository cityRepository;

    public CityServiceImpl(CityRepository cityRepository) {
        this.cityRepository = cityRepository;
    }

    @Override
    public CommonResponse getCities(int page, int size, String search, CityRequest request) {
        Pageable pageable = PageRequest.of(page, size);
        Page<CityEntity> result;

        String q = (search == null || search.isBlank()) ? null : search.trim();
        List<Long> provinceIds = request == null ? null : request.getProvinceIds();
        // Start from base active set, then apply search via repository method
        if (q == null) {
            result = cityRepository.findByDeleted("N", pageable);
        } else {
            result = cityRepository.searchActive("N", q, pageable);
        }

        // In-memory filter for provinceIds and codes if provided (keeps change minimal)
        if (provinceIds != null && !provinceIds.isEmpty()) {
            result = result.map(c -> c); // keep Page metadata, filter content below
            var filtered = result.getContent().stream()
                    .filter(c -> provinceIds.contains(c.getProvinceId()))
                    .toList();
            result = new org.springframework.data.domain.PageImpl<>(filtered, pageable, filtered.size());
        }

        if (request != null && request.getCodes() != null && !request.getCodes().isEmpty()) {
            var codes = request.getCodes();
            var filtered = result.getContent().stream()
                    .filter(c -> codes.contains(c.getCode()))
                    .toList();
            result = new org.springframework.data.domain.PageImpl<>(filtered, pageable, filtered.size());
        }

        if (result.isEmpty()) {
            return CommonResponse.error(new ErrorResponse(HttpStatus.NOT_FOUND.value(), "No cities found."),
                    HttpStatus.NOT_FOUND.value());
        }
        return CommonResponse.success(result);
    }
}


