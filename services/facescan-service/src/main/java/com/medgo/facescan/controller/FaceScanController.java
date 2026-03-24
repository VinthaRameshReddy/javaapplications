package com.medgo.facescan.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.medgo.commons.CommonResponse;
import com.medgo.crypto.annotation.DecryptBody;
import com.medgo.crypto.annotation.EncryptResponse;
import com.medgo.facescan.domain.request.*;
import com.medgo.facescan.service.FaceScanService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.util.StreamUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Slf4j
@Validated
@RequestMapping("/api/v1/faceScan")
@RestController
public class FaceScanController {

    private final FaceScanService faceScanService;

    public FaceScanController(FaceScanService faceScanService) {
        this.faceScanService = faceScanService;
    }

    @PostMapping("/eligibility")
    @EncryptResponse
    public CommonResponse checkEligibility(@Valid @DecryptBody(FaceScanEligibilityRequest.class) FaceScanEligibilityRequest request) {
        return faceScanService.checkEligibility(request.getMemberCode());
    }

    @PostMapping("/acceptTnc")
    @EncryptResponse
    public CommonResponse acceptTncAndStartSession(@Valid @DecryptBody(FaceScanTncRequest.class) FaceScanTncRequest request) {
        return faceScanService.acceptTncAndInitiateSession(request);
    }

    @PostMapping("/storeResult")
    @EncryptResponse
    public CommonResponse storeFaceScanResult(@Valid @DecryptBody(FaceScanResultRequest.class) FaceScanResultRequest request) throws JsonProcessingException {
        return faceScanService.storeFaceScanResult(request);
    }

    @PostMapping("/history")
    @EncryptResponse
    public CommonResponse getFaceScanHistory(@Valid @DecryptBody(FaceScanHistoryRequest.class) FaceScanHistoryRequest request) {
        return faceScanService.getFaceScanHistory(request);
    }

    @PostMapping("/fetchResult")
    @EncryptResponse
    public CommonResponse fetchFaceScanResult(@Valid @DecryptBody(FetchFaceScanResultRequest.class) FetchFaceScanResultRequest request) {
        return faceScanService.fetchFaceScanResult(request.getSessionId());
    }

    @PostMapping("/masterData")
    public CommonResponse getJsonDataTemplate() throws IOException {
        Resource resource = new ClassPathResource("facescan_masterdata.json");
        String json = StreamUtils.copyToString(resource.getInputStream(), StandardCharsets.UTF_8);
        // parse JSON into an Object so CommonResponse.data contains structured JSON, not a plain string
        ObjectMapper mapper = new ObjectMapper();
        Object data = mapper.readValue(json, Object.class);
        return CommonResponse.success(data);
    }
}
