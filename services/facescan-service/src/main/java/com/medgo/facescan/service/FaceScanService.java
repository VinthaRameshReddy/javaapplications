package com.medgo.facescan.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.medgo.commons.CommonResponse;
import com.medgo.facescan.domain.request.FaceScanHistoryRequest;
import com.medgo.facescan.domain.request.FaceScanResultRequest;
import com.medgo.facescan.domain.request.FaceScanTncRequest;
import jakarta.validation.Valid;

public interface FaceScanService  {
    CommonResponse checkEligibility(String memberCode);

    CommonResponse acceptTncAndInitiateSession(FaceScanTncRequest request);

    CommonResponse storeFaceScanResult(@Valid FaceScanResultRequest request) throws JsonProcessingException;

    CommonResponse fetchFaceScanResult(String sessionId);

    CommonResponse getFaceScanHistory(@Valid FaceScanHistoryRequest request);
}
