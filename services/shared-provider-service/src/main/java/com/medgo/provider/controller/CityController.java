package com.medgo.provider.controller;

import com.medgo.commons.CommonResponse;
import com.medgo.crypto.annotation.DecryptBody;
import com.medgo.crypto.annotation.EncryptResponse;
import com.medgo.provider.domain.request.CityRequest;
import com.medgo.provider.service.CityService;
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
public class CityController {

    private final CityService cityService;

    public CityController(CityService cityService) {
        this.cityService = cityService;
    }

    @PostMapping("/cities")
    @EncryptResponse
    public CommonResponse listCities(
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "10") @Min(1) int size,
            @RequestParam(required = false) String search,
             @DecryptBody(CityRequest.class)  CityRequest request
    ) {
        return cityService.getCities(page, size, search, request);
    }
}


