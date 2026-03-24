package com.medgo.virtualid.service.impl;

import com.google.gson.Gson;

import com.medgo.commons.CommonResponse;
import com.medgo.commons.ErrorResponse;
import com.medgo.virtualid.domain.response.VirtualIdResponseDto;
import com.medgo.virtualid.endpoint.VirtualIdEndPoint;
import com.medgo.virtualid.service.VirtualIdService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import retrofit2.Response;

import java.util.Collections;
import java.util.List;
@Slf4j
@RequiredArgsConstructor
@Service("virtualIdServiceImplIntegrationModule")
public class VirtualIdServiceImpl implements VirtualIdService {

    private final VirtualIdEndPoint endPoint;

    @Override
    public CommonResponse getGeneratedLink(String memberCode) {
        log.info("Generating virtual ID for member code: {}", memberCode);

        try {
            // Always pass skipMedgoValidation = true
            Response<VirtualIdResponseDto> response =
                    endPoint.getGeneratedLink(memberCode, true).execute();

            if (response.isSuccessful() && response.body() != null) {

                List<VirtualIdResponseDto.Data> dataList = response.body().getData();

                log.info(
                        "Virtual ID generated successfully for memberCode {}. Items returned: {}",
                        memberCode,
                        dataList == null ? 0 : dataList.size()
                );

                return CommonResponse.success(
                        dataList == null ? Collections.emptyList() : dataList
                );
            }

            log.warn("Empty response from Virtual ID service for memberCode {}", memberCode);

            return CommonResponse.error(
                    new ErrorResponse(404, "NO_DATA", "Your request has been queued."),
                    404
            );

        } catch (Exception e) {
            log.error(
                    "Exception while generating virtual ID for memberCode {}: {}",
                    memberCode, e.getMessage(), e
            );

            return CommonResponse.error(
                    new ErrorResponse(500, "INTERNAL_ERROR", e.getMessage()),
                    500
            );
        }
    }
}
