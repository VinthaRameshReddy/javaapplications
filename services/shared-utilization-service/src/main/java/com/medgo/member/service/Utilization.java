package com.medgo.member.service;


import com.medgo.commons.CommonResponse;
import com.medgo.member.domain.request.UtilizationRequest;

public interface Utilization {




    CommonResponse getUtilizationPdf(UtilizationRequest UtilizationRequest);

}

