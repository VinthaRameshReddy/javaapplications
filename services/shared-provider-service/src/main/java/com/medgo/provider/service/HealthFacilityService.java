package com.medgo.provider.service;

import com.medgo.provider.domain.request.HospitalRequest;
import com.medgo.provider.domain.request.ViewDoctorHospitalRequest;
import com.medgo.commons.CommonResponse;

public interface HealthFacilityService {
    CommonResponse getHospitalsList(int page, int size, String search, HospitalRequest request);
    CommonResponse getViewDoctorHospitalV2(int page, int size, ViewDoctorHospitalRequest request);
}