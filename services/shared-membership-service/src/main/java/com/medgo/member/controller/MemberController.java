package com.medgo.member.controller;


import com.medgo.commons.CommonResponse;
import com.medgo.crypto.annotation.EncryptResponse;
import com.medgo.member.service.MemberService;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/v1")
public class MemberController {
    private final MemberService memberService;

    public MemberController(MemberService memberService) {
        this.memberService = memberService;
    }

    @GetMapping("/member")
    @EncryptResponse
    public CommonResponse findMemberByCode(
            @RequestParam("userCode") String userCode,
            @RequestParam(value = "userDependentId", required = false) Long userDependentId) {
        CommonResponse response = memberService.findMemberByCode(userCode, userDependentId);
        return response; // always return 200
    }

    @GetMapping("/member/dependent")
    @EncryptResponse
    public CommonResponse findDependentsByPrincipalCode(
            @RequestParam("userCode") String userCode) {
        CommonResponse response = memberService.findDependentsByPrincipalCode(userCode);
        return response;
    }

    @GetMapping("/member/maternity")
    @EncryptResponse
    public CommonResponse findMaternityBenefitsByCode(
            @RequestParam("userCode") String userCode) {
        CommonResponse response = memberService.findMaternityBenefitsByCode(userCode);
        return response;
    }
}
