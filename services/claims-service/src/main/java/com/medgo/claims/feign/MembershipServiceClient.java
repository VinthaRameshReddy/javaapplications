//package com.medgo.claims.feign;
//
//import com.medgo.claims.config.FeignClientConfig;
//import org.springframework.cloud.openfeign.FeignClient;
//import org.springframework.web.bind.annotation.GetMapping;
//import org.springframework.web.bind.annotation.RequestParam;
//
//@FeignClient(
//        name = "shared-membership-service",
//        url = "${shared.membership.service.url}",
//        configuration = FeignClientConfig.class
//)
//public interface MembershipServiceClient {
//
//    @GetMapping("/membership/v1/member")
//    Object findMemberByCode(
//            @RequestParam("userCode") String userCode,
//            @RequestParam(value = "userDependentId", required = false) Long userDependentId);
//}
//
