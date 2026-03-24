package com.medgo.provider.controller;

import com.medgo.commons.CommonResponse;
import com.medgo.crypto.annotation.DecryptBody;
import com.medgo.crypto.annotation.EncryptResponse;
import com.medgo.provider.feign.SharedProviderServiceClient;
import com.medgo.provider.model.HospitalRequest;
import com.medgo.provider.model.ViewDoctorHospitalRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/provider")
public class ProviderProxyController {

    private final SharedProviderServiceClient sharedProviderServiceClient;

    /**
     * Get Doctor-Hospital List (proxied to shared-provider-service)
     */
    @PostMapping("/doctor")
    @EncryptResponse
    public CommonResponse doctor(
            @RequestParam(defaultValue = "0")
            @Min(value = 0, message = "Page minimum value is 0.") int page,

            @RequestParam(defaultValue = "5")
            @Min(value = 5, message = "Size minimum value is 5.") int size,

             @DecryptBody(ViewDoctorHospitalRequest.class)
             ViewDoctorHospitalRequest request) {

        return sharedProviderServiceClient.doctor(page, size, request);
    }

    /**
     * Get Hospital List (proxied to shared-provider-service)
     */
    @PostMapping("/hospital")
    @EncryptResponse
    public CommonResponse hospital(
            @RequestParam(defaultValue = "0")
            @Min(value = 0, message = "Page minimum value is 0.") int page,

            @RequestParam(defaultValue = "10")
            @Min(value = 5, message = "Size minimum value is 5.") int size,

            @RequestParam(required = false) String search,

             @DecryptBody(HospitalRequest.class)
            HospitalRequest request) {

        return sharedProviderServiceClient.hospital(page, size, search, request);
    }
}
