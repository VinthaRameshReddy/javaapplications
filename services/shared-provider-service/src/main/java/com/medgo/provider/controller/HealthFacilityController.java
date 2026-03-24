package com.medgo.provider.controller;

import com.medgo.provider.constants.ClaimsConstants;
import com.medgo.provider.domain.request.HospitalRequest;
import com.medgo.provider.domain.request.ViewDoctorHospitalRequest;
import com.medgo.provider.service.HealthFacilityService;
import com.medgo.commons.CommonResponse;
import com.medgo.crypto.annotation.EncryptResponse;
import com.medgo.crypto.annotation.DecryptBody;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/v1")

public class HealthFacilityController {

    private final HealthFacilityService healthFacilityService;

    @Autowired
    public HealthFacilityController(HealthFacilityService healthFacilityService) {
        this.healthFacilityService = healthFacilityService;
    }

    /**
     * View Doctor Hospital – V2
     */
    @PostMapping("/doctor")
    @EncryptResponse
    public CommonResponse getViewDoctorHospitalV2(
            @RequestParam(defaultValue = "0")
            @Min(value = 0, message = "Page minimum value is 0.") int page,

            @RequestParam(defaultValue = "5")
            @Min(value = 5, message = "Size minimum value is 5.") int size,

             @DecryptBody(ViewDoctorHospitalRequest.class)
             ViewDoctorHospitalRequest request) {

        // traceId is now handled automatically inside CommonResponse
        return healthFacilityService.getViewDoctorHospitalV2(page, size, request);
    }

    /**
     * Hospitals List
     */
    @PostMapping("/hospital")
    @EncryptResponse
    public CommonResponse getHospitalsList(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String search,

             @DecryptBody(HospitalRequest.class)
             HospitalRequest request) {

        return healthFacilityService.getHospitalsList(page, size, search, request);
    }
}
