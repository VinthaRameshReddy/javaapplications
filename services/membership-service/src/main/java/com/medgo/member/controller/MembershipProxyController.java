package com.medgo.member.controller;


import com.medgo.commons.CommonResponse;
import com.medgo.crypto.annotation.EncryptResponse;
import com.medgo.member.feign.SharedMembershipServiceClient;
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
@RequestMapping("/api/v1/membership")
@RequiredArgsConstructor

public class MembershipProxyController {

    private final SharedMembershipServiceClient sharedMembershipServiceClient;
    private final MemberCodeValidationService memberCodeValidationService;




    @GetMapping("/memberProfile")
    @EncryptResponse
    public CommonResponse findMemberByCode(
            @RequestParam("userCode") String userCode,
            @RequestParam(value = "userDependentId", required = false) Long userDependentId,
            HttpServletRequest request) {
        memberCodeValidationService.validateMemberCode(userCode, request);
        return sharedMembershipServiceClient.findMemberByCode(userCode, userDependentId);
    }


    @GetMapping("/dependent")
    @EncryptResponse
    public CommonResponse findDependentsByPrincipalCode(
            @RequestParam("userCode") String userCode,
            HttpServletRequest request) {
        memberCodeValidationService.validateMemberCode(userCode, request);
        return sharedMembershipServiceClient.findDependentsByPrincipalCode(userCode);
    }

    @GetMapping("/maternity")
    @EncryptResponse
    public CommonResponse findMaternityBenefitsByCode(
            @RequestParam("userCode") String userCode,
            HttpServletRequest request) {
        memberCodeValidationService.validateMemberCode(userCode, request);
        return sharedMembershipServiceClient.findMaternityBenefitsByCode(userCode);
    }





}
