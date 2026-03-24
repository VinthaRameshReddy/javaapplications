package com.medgo.member.feign;

import com.medgo.commons.CommonResponse;
import com.medgo.member.config.FeignClientConfig;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;


@FeignClient(
        name = "shared-membership-service",
        url = "${shared.membership.service.url}",
        configuration = FeignClientConfig.class)

public interface SharedMembershipServiceClient {



    @GetMapping("/membership/v1/member")
    CommonResponse findMemberByCode(
            @RequestParam("userCode") String userCode,
            @RequestParam(value = "userDependentId", required = false) Long userDependentId);


    @GetMapping("/membership/v1/member/dependent")
    CommonResponse findDependentsByPrincipalCode(@RequestParam("userCode") String userCode);


    @GetMapping("/membership/v1/member/maternity")
    CommonResponse findMaternityBenefitsByCode(
            @RequestParam("userCode") String userCode);







}
