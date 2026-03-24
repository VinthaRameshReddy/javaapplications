package com.medgo.virtualid.service;


import com.medgo.commons.CommonResponse;
import com.medgo.virtualid.domain.response.VirtualIdResponseDto;

public interface VirtualIdService extends BaseService {

    CommonResponse getGeneratedLink(String memberCode);
}
