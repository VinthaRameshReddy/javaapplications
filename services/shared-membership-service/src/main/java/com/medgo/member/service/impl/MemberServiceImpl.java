package com.medgo.member.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.medgo.commons.CommonResponse;
import com.medgo.commons.ErrorResponse;
import com.medgo.member.domain.entity.membership.MaternityBenefitsEntity;
import com.medgo.member.domain.entity.membership.MembershipEntity;
import com.medgo.member.domain.response.MembershipResponse;
import com.medgo.member.domain.response.UserDependentResponse;
import com.medgo.member.feign.FileServiceFeignClient;
import com.medgo.member.mapper.MemberMapper;
import com.medgo.member.model.CoreConstants;
import com.medgo.member.model.CoreUtils;
import com.medgo.member.repository.membership.DataPrivacyTaggingRepository;
import com.medgo.member.repository.membership.MaternityBenefitsRepository;
import com.medgo.member.repository.membership.MembershipRepository;
import com.medgo.member.service.MemberService;
import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toList;

@Slf4j
@Service
@RequiredArgsConstructor
public class MemberServiceImpl implements MemberService {

    private final ObjectMapper objectMapper;
    private final MembershipRepository membershipRepository;
    private final DataPrivacyTaggingRepository dataPrivacyTaggingRepository;
    private final MaternityBenefitsRepository maternityBenefitsRepository;
    private final MemberMapper mapper;
    private final FileServiceFeignClient fileServiceFeignClient;

    /**
     * Fetch membership details using member code.
     */
    @Override
    public CommonResponse findMemberByCode(String userCode, Long userDependentId) {
        try {

            MembershipEntity member = membershipRepository.findByMemberCode(userCode);
            if (member == null) {
                return CommonResponse.error(
                        new ErrorResponse(404, CoreConstants.MEMBER_NOT_FOUND),
                        404);
            }

            MembershipResponse membershipResponse = mapper.toMembershipResponse(member);
            return CommonResponse.success(membershipResponse);
        } catch (Exception e) {
            log.error("Failed to retrieve membership: {}", e.getMessage());
            return CommonResponse.error(
                    new ErrorResponse(500, "Failed to retrieve membership: " + e.getMessage()),
                    500);
        }
    }


    /**
     * Fetch dependents for a principal member using the same membership table.
     * Dependents are those where principalCode = given member code.
     */
//    @Override
//    public CommonResponse findDependentsByPrincipalCode(String userCode) {
//        try {
//            log.info("Fetching dependents for principalCode: {}", userCode);
//
//            // Get dependents whose principalCode = given memberCode
//            List<MembershipEntity> dependents = membershipRepository.findByPrincipalCode(userCode);
//
//            if (dependents == null || dependents.isEmpty()) {
//
//                return CommonResponse.error(
//                        new ErrorResponse(404, CoreConstants.DEPENDENTS_NOT_FOUND),
//                        404);
//            }
//
//            List<UserDependentResponse> responses = new ArrayList<>();
//
//            for (MembershipEntity dependent : dependents) {
//                UserDependentResponse ud = new UserDependentResponse();
//                ud.setMembership(mapper.toMembershipResponse(dependent));
//                ud.setDependentCode(dependent.getMemberCode());
//                ud.setStatus(dependent.getMemStatus());
//                ud.setId(Long.valueOf(dependent.getMemberCode()));
//                // Fetch photo URL
//                try {
//                    Map<String, String> tags = new HashMap<>();
//                    tags.put("tableName", "USER_PHOTO");
//                    tags.put("id", dependent.getMemberCode());
//                    List<String> links = fileServiceFeignClient.findLinksByTags(tags);
//                    ud.setPhotoUrl(links != null && !links.isEmpty() ? links.get(0) : null);
//                } catch (FeignException e) {
//                    log.warn("Failed to fetch photo URL for dependent {}: {}", dependent.getMemberCode(), e.getMessage());
//                    ud.setPhotoUrl(null);
//                }
//
//                // Consent check
//                boolean isConsented = dataPrivacyTaggingRepository.existsByMemberCode(dependent.getMemberCode());
//                if (!isConsented && dependent.getBirthDate() != null) {
//                    isConsented = CoreUtils.calculateAge(dependent.getBirthDate().toLocalDate()) < 18;
//                }
//                ud.setConsented(isConsented);
//
//                responses.add(ud);
//            }
//            return CommonResponse.success(responses);
//
////            return CommonResponse.success(objectMapper.convertValue(responses, List.class));
//
//        } catch (Exception e) {
//            log.error("Failed to retrieve dependents: {}", e.getMessage(), e);
//            return CommonResponse.error(
//                    new ErrorResponse(500, "Failed to retrieve dependents: " + e.getMessage()),
//                    500);
//        }
//    }


    @Override
    public CommonResponse findDependentsByPrincipalCode(String userCode) {
        try {
            log.info("Fetching dependents for principalCode: {}", userCode);

            // Get dependents whose principalCode = given memberCode
            List<MembershipEntity> dependents = membershipRepository.findByPrincipalCode(userCode);

            if (dependents == null || dependents.isEmpty()) {
                return CommonResponse.error(
                        new ErrorResponse(404, CoreConstants.DEPENDENTS_NOT_FOUND),
                        404);
            }

            List<UserDependentResponse> responses = new ArrayList<>();

            for (MembershipEntity dependent : dependents) {
                UserDependentResponse ud = new UserDependentResponse();
                ud.setMembership(mapper.toMembershipResponse(dependent));
                ud.setDependentCode(dependent.getMemberCode());
                ud.setStatus(dependent.getMemStatus());
                ud.setId(Long.valueOf(dependent.getMemberCode()));

                // Fetch photo URL
                try {
                    Map<String, String> tags = new HashMap<>();
                    tags.put("tableName", "USER_PHOTO");
                    tags.put("id", dependent.getMemberCode());
                    List<String> links = fileServiceFeignClient.findLinksByTags(tags);
                    ud.setPhotoUrl(links != null && !links.isEmpty() ? links.get(0) : null);
                } catch (FeignException e) {
                    log.warn("Failed to fetch photo URL for dependent {}: {}", dependent.getMemberCode(), e.getMessage());
                    ud.setPhotoUrl(null);
                }

                // Consent check
                boolean isConsented = dataPrivacyTaggingRepository.existsByMemberCode(dependent.getMemberCode());
                if (!isConsented && dependent.getBirthDate() != null) {
                    isConsented = CoreUtils.calculateAge(dependent.getBirthDate().toLocalDate()) < 18;
                }
                ud.setConsented(isConsented);

                responses.add(ud);
            }
//            Map<String, Object> successPayload = new HashMap<>();
//            successPayload.put("", responses);  // Raw List<UserDependentResponse> - serializes to JSON array
//
//            List<Object> valueList = successPayload.values().stream()
//                    .collect(toList());
//            return CommonResponse.success(valueList);

            return CommonResponse.success(responses);

        } catch (Exception e) {
            log.error("Failed to retrieve dependents: {}", e.getMessage(), e);
            return CommonResponse.error(
                    new ErrorResponse(500, "Failed to retrieve dependents: " + e.getMessage()),
                    500);
        }
    }












    /**
     * Fetch maternity benefits by member code.
     */
    @Override
    @Transactional("membershipTransactionManager")
    public CommonResponse findMaternityBenefitsByCode(String userCode) {
        try {
            MembershipEntity member = membershipRepository.findByMemberCode(userCode);
            if (member == null) {
                return CommonResponse.error(
                        new ErrorResponse(404, CoreConstants.MEMBER_NOT_FOUND),
                        404);
            }

            List<MaternityBenefitsEntity> benefits =
                    maternityBenefitsRepository.getMaternityBenefitsByModelEntity(member.getAccountCode());

            if (benefits == null || benefits.isEmpty()) {
                return CommonResponse.error(
                        new ErrorResponse(404, "Maternity benefits not found"),
                        404);
            }


            // Assuming you want the first one:
            return CommonResponse.success(mapper.toMaternityBenefitsResponse(benefits.get(0)));

        } catch (Exception e) {
            log.error("Failed to retrieve maternity benefits: {}", e.getMessage(), e);
            return CommonResponse.error(
                    new ErrorResponse(500, "Failed to retrieve maternity benefits: " + e.getMessage()),
                    500);
        }
    }
}
