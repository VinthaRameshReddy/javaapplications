package com.medgo.loaservice.controller;

import com.medgo.commons.CommonResponse;
import com.medgo.commons.ErrorResponse;
import com.medgo.crypto.annotation.DecryptBody;
import com.medgo.loaservice.domain.dto.request.LoaRequestDTO;
import com.medgo.loaservice.domain.dto.response.LoaDownloadResponseDTO;
import com.medgo.loaservice.domain.enums.SystemOriginEnum;
import com.medgo.loaservice.feign.MedicardApiClient;
import com.medgo.loaservice.service.MemberCodeValidationService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;


@Slf4j
@RestController
@RequiredArgsConstructor
public class LoaController {

    private final MedicardApiClient medicardApiClient;
    private final MemberCodeValidationService memberCodeValidationService;

    @PostMapping("/transaction")
    public CommonResponse requestLoa(@Valid @DecryptBody(LoaRequestDTO.class) LoaRequestDTO request) {
        try {

            if (request != null && request.getOriginMemberCode() != null && !request.getOriginMemberCode().isBlank()) {
                memberCodeValidationService.validateMemCodeOrThrow(request.getOriginMemberCode());
            }
            Object response = medicardApiClient.requestLoa(request);
            return CommonResponse.success(response);
        } catch (Exception e) {
            log.error("Error calling Medicard API: {}", e.getMessage(), e);
            throw e;
        }
    }


    @GetMapping("/transaction/history")
    public CommonResponse getLoaHistory(
            @RequestParam(value = "page", defaultValue = "0") @Min(value = 0, message = "page must be >= 0") int page,
            @RequestParam(value = "size", defaultValue = "5") @Min(value = 5, message = "size must be >= 5") int size,
            @RequestParam("originMemberCode")
            @Size(max = 50, message = "originMemberCode must not exceed 50 characters") String originMemberCode,
            @RequestParam(value = "hospitalCode", required = false)
            @Size(max = 50, message = "hospitalCode must not exceed 50 characters") String hospitalCode) {

        memberCodeValidationService.validateMemCodeOrThrow(originMemberCode);
        Object response = medicardApiClient.getLoaHistory(page, size, originMemberCode, hospitalCode);
        return CommonResponse.success(response);
    }


    @GetMapping("/transaction/historyDetails/{id}")
    public CommonResponse getLoaHistoryDetails(
            @PathVariable("id") Long id,
            @RequestParam("systemOrigin") String systemOrigin,
            @RequestParam("originMemberCode")
            @Size(max = 50, message = "originMemberCode must not exceed 50 characters") String originMemberCode) {
        SystemOriginEnum origin = SystemOriginEnum.fromCode(systemOrigin);
        if (origin == null) {
            log.error("Invalid systemOrigin: {}. Must be MCAP or MACE", systemOrigin);
            ErrorResponse error = new ErrorResponse(400, "INVALID_SYSTEM_ORIGIN",
                    "Invalid systemOrigin. Must be MCAP or MACE", Collections.emptyList());
            return CommonResponse.error(error, 400);
        }

        memberCodeValidationService.validateMemCodeOrThrow(originMemberCode);
        Object response = medicardApiClient.getLoaHistoryDetails(id, systemOrigin, originMemberCode);
        return CommonResponse.success(response);
    }


    @GetMapping("/download/loa")
    public CommonResponse downloadLoa(
            @RequestParam("systemOrigin") String systemOrigin,
            @RequestParam("requestId") Long requestId,
            @RequestParam("originMemberCode")
            @Size(max = 50, message = "originMemberCode must not exceed 50 characters") String originMemberCode) {
        SystemOriginEnum origin = SystemOriginEnum.fromCode(systemOrigin);
        if (origin == null) {
            log.error("Invalid systemOrigin: {}. Must be MCAP or MACE", systemOrigin);
            ErrorResponse error = new ErrorResponse(400, "INVALID_SYSTEM_ORIGIN",
                    "Invalid systemOrigin. Must be MCAP or MACE", Collections.emptyList());
            return CommonResponse.error(error, 400);
        }

        try {
            memberCodeValidationService.validateMemCodeOrThrow(originMemberCode);
            LoaDownloadResponseDTO response = medicardApiClient.downloadLoa(systemOrigin, requestId, originMemberCode);

            if (response == null) {
                log.error("Response is null for requestId: {}", requestId);
                ErrorResponse error = new ErrorResponse(500, "INTERNAL_ERROR",
                        "Failed to retrieve LOA download response", Collections.emptyList());
                return CommonResponse.error(error, 500);
            }

            log.info("Received response - statusCode: {}, statusName: {}, data present: {}", 
                    response.getStatusCode(), 
                    response.getStatusName(),
                    response.getData() != null);

            return CommonResponse.success(response);

        } catch (Exception e) {
            log.error("Error downloading LOA form: {}", e.getMessage(), e);
            ErrorResponse error = new ErrorResponse(500, "INTERNAL_ERROR",
                    "Error downloading LOA form: " + e.getMessage(), Collections.emptyList());
            return CommonResponse.error(error, 500);
        }
    }



}


