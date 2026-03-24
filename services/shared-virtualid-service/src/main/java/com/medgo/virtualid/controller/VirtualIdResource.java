package com.medgo.virtualid.controller;

import com.medgo.commons.CommonResponse;
import com.medgo.crypto.annotation.EncryptResponse;
import com.medgo.virtualid.service.VirtualIdService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequiredArgsConstructor
public class VirtualIdResource {

    private final VirtualIdService service;

    @GetMapping("/generate-link")
    @EncryptResponse
    public CommonResponse getGeneratedLink(@RequestParam("memberCode") String memberCode) {
        // Service internally uses skipMedgoValidation = true
        return service.getGeneratedLink(memberCode);
    }
}
