package com.medgo.member.repository.utilization;

import com.medgo.member.domain.request.UtilizationRequest;
import com.medgo.member.domain.response.UtilizationResponse;

import java.util.List;

public interface UtilizationLegacyRepositoryCustom {
//    List<UtilizationLegacyEntity> findUtilizationData(UtilizationRequest request);

    List<UtilizationResponse> findUtilizationDataV6(UtilizationRequest request);
}
