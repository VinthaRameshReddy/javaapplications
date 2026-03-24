package com.medgo.provider.service;

import com.medgo.commons.CommonResponse;
import com.medgo.provider.domain.request.CityRequest;

public interface CityService {
    CommonResponse getCities(int page, int size, String search, CityRequest request);
}


