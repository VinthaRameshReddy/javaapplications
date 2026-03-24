package com.medgo.provider.feign;

import com.medgo.commons.CommonResponse;
import com.medgo.provider.config.FeignClientConfig;
import com.medgo.provider.model.HospitalRequest;
import com.medgo.provider.model.ViewDoctorHospitalRequest;
import jakarta.validation.Valid;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(
        name = "shared-provider-service",
        url = "${shared.provider.service.url}",
        configuration = FeignClientConfig.class
)
public interface SharedProviderServiceClient {

    @PostMapping("/provider/v1/doctor")
    CommonResponse doctor(
            @RequestParam("page") int page,
            @RequestParam("size") int size,
            @Valid @RequestBody ViewDoctorHospitalRequest request
    );

    @PostMapping("/provider/v1/hospital")
    CommonResponse hospital(
            @RequestParam("page") int page,
            @RequestParam("size") int size,
            @RequestParam(value = "search", required = false) String search,
            @Valid @RequestBody HospitalRequest request
    );
}
