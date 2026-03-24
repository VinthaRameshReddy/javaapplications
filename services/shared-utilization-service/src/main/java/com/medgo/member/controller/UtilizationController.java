package com.medgo.member.controller;

import com.medgo.commons.CommonResponse;
import com.medgo.crypto.annotation.DecryptBody;
import com.medgo.crypto.annotation.EncryptResponse;
import com.medgo.member.domain.request.UtilizationRequest;
import com.medgo.member.service.Utilization;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;



@RestController
@RequestMapping("/v1")
public class UtilizationController {

    @Autowired
    Utilization utilization;

    @PostMapping("/member/utilization")
    @EncryptResponse
    public CommonResponse getUtilizationPdf(@DecryptBody(UtilizationRequest.class) UtilizationRequest utilizationRequest) {
        CommonResponse response = utilization.getUtilizationPdf(utilizationRequest);
        return response;
    }

}