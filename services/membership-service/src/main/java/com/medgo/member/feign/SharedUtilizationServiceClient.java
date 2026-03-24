package com.medgo.member.feign;

import com.medgo.commons.CommonResponse;

import com.medgo.member.config.FeignClientConfig;
import com.medgo.member.model.UtilizationRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(
        name = "shared-utilization-service",
        url = "${shared.utilization.service.url}",
        configuration = FeignClientConfig.class
)
public interface SharedUtilizationServiceClient {

    @PostMapping("/utilization/v1/optimizedspcall")
    CommonResponse getUtilizationData(@RequestBody UtilizationRequest request);


    @PostMapping("/utilization/v1/member/utilization")
    CommonResponse getUtilizationPdf(@RequestBody UtilizationRequest request);
}
