package com.medgo.member.controller;

import com.medgo.commons.CommonResponse;
import com.medgo.crypto.annotation.EncryptResponse;
import com.medgo.member.feign.SharedVirtualIdServiceClient;
import com.medgo.member.service.MemberCodeValidationService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor

public class VirtualIdFeignProxyController {

    private final SharedVirtualIdServiceClient sharedVirtualIdServiceClient;
    private final MemberCodeValidationService memberCodeValidationService;

    @GetMapping("/virtual-id")
    @EncryptResponse
    public CommonResponse getGeneratedLink(
            @RequestParam("memberCode") String memberCode,
            HttpServletRequest request) {
        memberCodeValidationService.validateMemberCode(memberCode, request);
        return sharedVirtualIdServiceClient.getGeneratedLink(memberCode);
    }

}
