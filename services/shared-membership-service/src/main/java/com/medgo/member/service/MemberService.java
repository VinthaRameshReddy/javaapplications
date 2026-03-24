package com.medgo.member.service;


import com.medgo.commons.CommonResponse;

public interface MemberService {
    CommonResponse findMemberByCode(String userCode, Long userDependentId);
    CommonResponse findDependentsByPrincipalCode(String userCode);
    CommonResponse findMaternityBenefitsByCode(String userCode);
}