package com.medgo.member.feign;

import com.medgo.commons.CommonResponse;

import com.medgo.member.config.FeignClientConfig;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;


@FeignClient(
        name = "shared-virtualid-service",
        url = "${shared.virtualid.service.url}",
        configuration = FeignClientConfig.class)

public interface SharedVirtualIdServiceClient {


    @GetMapping("/virtual-id/generate-link")
    public CommonResponse getGeneratedLink(@RequestParam("memberCode") String memberCode) ;



}
