package com.medgo.member.controller;

import com.medgo.commons.CommonResponse;
import com.medgo.crypto.annotation.DecryptBody;
import com.medgo.crypto.annotation.EncryptResponse;
import com.medgo.member.feign.SharedUtilizationServiceClient;
import com.medgo.member.model.UtilizationRequest;
import com.medgo.member.service.MemberCodeValidationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/v1/utilization")
@RequiredArgsConstructor
public class UtilizationProxyController {

    private final SharedUtilizationServiceClient sharedUtilizationServiceClient;
    private final MemberCodeValidationService memberCodeValidationService;

//    @PostMapping("/optimizedspcall")
//    @EncryptResponse
//    public CommonResponse getUtilizationData(@DecryptBody(UtilizationRequest.class) UtilizationRequest request,@RequestParam int page,
//                                             @RequestParam int size) {
//        request.setPage(page);
//        request.setSize(size);
//        return sharedUtilizationServiceClient.getUtilizationData(request,page,size);
//    }

    @PostMapping("/optimizedspcall")
    @EncryptResponse
    public CommonResponse getUtilizationData(
            @DecryptBody(UtilizationRequest.class) UtilizationRequest request) {
        memberCodeValidationService.validateAndPrepareUtilizationRequest(request);
        return sharedUtilizationServiceClient.getUtilizationData(request);
    }


    @PostMapping("/utilizationpdf")
    @EncryptResponse
    public CommonResponse getUtilizationPdf(@DecryptBody(UtilizationRequest.class) UtilizationRequest request) {
        memberCodeValidationService.validateMemberCodeFromRequest(request);
        return sharedUtilizationServiceClient.getUtilizationPdf(request);
    }
}
