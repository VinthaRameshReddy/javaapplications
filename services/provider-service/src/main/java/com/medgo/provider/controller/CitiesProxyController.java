package com.medgo.provider.controller;

import com.medgo.commons.CommonResponse;
import com.medgo.crypto.annotation.DecryptBody;
import com.medgo.crypto.annotation.EncryptResponse;
import com.medgo.provider.feign.CitiesClient;
import com.medgo.provider.model.CityRequest;
import com.medgo.provider.model.ViewDoctorHospitalRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1")
@Validated
public class CitiesProxyController {

    private final CitiesClient citiesClient;

    public CitiesProxyController(CitiesClient citiesClient) {
        this.citiesClient = citiesClient;
    }

    @PostMapping("/cities")
    @EncryptResponse
    public CommonResponse listCities(
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "10") @Min(1) int size,
            @RequestParam(required = false) String search,
             @DecryptBody(CityRequest.class)
             CityRequest request
    ) {
        return citiesClient.listCities(page, size, search, request);
    }
}


