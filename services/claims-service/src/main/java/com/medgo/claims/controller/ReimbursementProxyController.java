package com.medgo.claims.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.medgo.claims.domain.dto.request.ReimbursementDetailsRequest;
import com.medgo.claims.domain.dto.request.ReimbursementResubmitRequest;
import com.medgo.claims.domain.dto.request.ReimbursementSubmissionRequest;
import com.medgo.claims.feign.ReimbursementsServiceClient;
import com.medgo.claims.service.*;
import com.medgo.claims.service.MemberCodeValidationService;
import com.medgo.commons.CommonResponse;
import com.medgo.crypto.annotation.DecryptBody;
import com.medgo.crypto.annotation.EncryptResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolation;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/api/v1/reimbursement")
@RequiredArgsConstructor
public class ReimbursementProxyController {
    
    private final ReimbursementsServiceClient reimbursementsServiceClient;
    private final MemberCodeValidationService memberCodeValidationService;
    private final ClaimNatureService claimNatureService;
    private final BankMasterService bankMasterService;
    private final ObjectMapper objectMapper;
    private final jakarta.validation.Validator validator;

    @GetMapping("/history")
    @EncryptResponse
    public CommonResponse reimbursementHistory(
            @RequestParam(name = "memberCode") String memberCode) {
        memberCodeValidationService.validateMemberCode(memberCode);
        return reimbursementsServiceClient.getReimbursementHistory(memberCode);
    }

    @PostMapping("/claimNature")
    @EncryptResponse
    public CommonResponse getClaimNature() {
        Map<String, List<String>> data = claimNatureService.getClaimNatureByServiceType();
        return CommonResponse.success(data);
    }

    @PostMapping("/bankMaster")
    @EncryptResponse
    public CommonResponse getBankMaster() {
        Map<String, Object> data = bankMasterService.getEnabledBanks();
        return CommonResponse.success(data);
    }

    @PostMapping("/viewAmountBreakDown")
    @EncryptResponse
    public CommonResponse getRequestDetails(@Valid @DecryptBody(ReimbursementDetailsRequest.class) ReimbursementDetailsRequest request) {
        Map<String, String> feignRequest = new HashMap<>();
        feignRequest.put("controlCode", request.getControlCode());
        feignRequest.put("status", request.getStatus());
        
        if (request.getEntryCode() != null && !request.getEntryCode().isBlank()) {
            feignRequest.put("entryCode", request.getEntryCode());
        }
        
        return reimbursementsServiceClient.getRequestDetails(feignRequest);
    }

    @PostMapping(value = "/submit", consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
//    @EncryptResponse
    public CommonResponse submitReimbursement(
            @RequestPart("request") String requestJson,
            @RequestPart(value = "serviceInvoice", required = false) List<MultipartFile> serviceInvoice,
            @RequestPart(value = "itemizedBreakdown", required = false) List<MultipartFile> itemizedBreakdown,
            @RequestPart(value = "medcert", required = false) List<MultipartFile> medcert,
            @RequestPart(value = "operativeTechnique", required = false) List<MultipartFile> operativeTechnique,
            @RequestPart(value = "hospitalStatement", required = false) List<MultipartFile> hospitalStatement,
            @RequestPart(value = "certificateOfLiveBirth", required = false) List<MultipartFile> certificateOfLiveBirth,
            @RequestPart(value = "liveBirth", required = false) List<MultipartFile> liveBirth,
            @RequestPart(value = "doctorPrescription", required = false) List<MultipartFile> doctorPrescription,
            @RequestPart(value = "irDocument", required = false) List<MultipartFile> irDocument,
            @RequestPart(value = "irDocumentAccredited", required = false) List<MultipartFile> irDocumentAccredited,
            @RequestPart(value = "bankDocuments", required = false) MultipartFile bankDocuments) {
        
        log.info("=== Reimbursement submission request received ===");

        ReimbursementSubmissionRequest request;
        try {
            request = objectMapper.readValue(requestJson, ReimbursementSubmissionRequest.class);
        } catch (JsonProcessingException e) {
            log.error("Invalid JSON in request part: {}", e.getMessage());
            throw new IllegalArgumentException("Invalid JSON in request part: " + e.getMessage(), e);
        }

        if (request.getMemberCode() != null && !request.getMemberCode().isEmpty()) {
            HttpServletRequest httpRequest = memberCodeValidationService.getCurrentRequest();
            memberCodeValidationService.validateMemberCode(request.getMemberCode(), httpRequest);
        }

        Set<ConstraintViolation<ReimbursementSubmissionRequest>> violations = validator.validate(request);
        if (!violations.isEmpty()) {
            String messages = violations.stream().map(ConstraintViolation::getMessage).collect(Collectors.joining("; "));
            log.warn("Validation failed for reimbursement submission: {}", messages);
            throw new IllegalArgumentException("Validation failed: " + messages);
        }

        log.debug("Request for member: {}, serviceType: {}", request.getMemberCode(), request.getServiceType());
        log.debug("Request JSON length: {}", requestJson.length());
        log.debug("File counts - serviceInvoice: {}, itemizedBreakdown: {}, medcert: {}", 
                sizeOf(serviceInvoice), sizeOf(itemizedBreakdown), sizeOf(medcert));

        try {
            return reimbursementsServiceClient.submitReimbursement(
                    requestJson,
                    nullIfEmpty(serviceInvoice),
                    nullIfEmpty(itemizedBreakdown),
                    nullIfEmpty(medcert),
                    nullIfEmpty(operativeTechnique),
                    nullIfEmpty(hospitalStatement),
                    nullIfEmpty(certificateOfLiveBirth),
                    nullIfEmpty(liveBirth),
                    nullIfEmpty(doctorPrescription),
                    nullIfEmpty(irDocument),
                    nullIfEmpty(irDocumentAccredited),
                    bankDocuments
            );
        } catch (Exception e) {
            log.error("Failed to process reimbursement submission: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to process reimbursement submission: " + e.getMessage(), e);
        }
    }

    @PostMapping(value = "/resubmit", consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
//    @EncryptResponse
    public CommonResponse resubmitReimbursement(
            @RequestPart("request")  ReimbursementResubmitRequest request,
            @RequestPart(value = "serviceInvoice", required = false) List<MultipartFile> serviceInvoice,
            @RequestPart(value = "itemizedBreakdown", required = false) List<MultipartFile> itemizedBreakdown,
            @RequestPart(value = "medcert", required = false) List<MultipartFile> medcert,
            @RequestPart(value = "operativeTechnique", required = false) List<MultipartFile> operativeTechnique,
            @RequestPart(value = "hospitalStatement", required = false) List<MultipartFile> hospitalStatement,
            @RequestPart(value = "certificateOfLiveBirth", required = false) List<MultipartFile> certificateOfLiveBirth,
            @RequestPart(value = "liveBirth", required = false) List<MultipartFile> liveBirth,
            @RequestPart(value = "doctorPrescription", required = false) List<MultipartFile> doctorPrescription,
            @RequestPart(value = "irDocument", required = false) List<MultipartFile> irDocument,
            @RequestPart(value = "irDocumentAccredited", required = false) List<MultipartFile> irDocumentAccredited,
            @RequestPart(value = "bankDocuments", required = false) MultipartFile bankDocuments) {
        
        log.info("=== Reimbursement resubmission request received ===");
        log.debug("Request for control code: {}, member: {}", request.getControlCode(), request.getMemberCode());

        if (request.getMemberCode() != null && !request.getMemberCode().isEmpty()) {
            HttpServletRequest httpRequest = memberCodeValidationService.getCurrentRequest();
            memberCodeValidationService.validateMemberCode(request.getMemberCode(), httpRequest);
        }

        try {
            String requestJson = objectMapper.writeValueAsString(request);
            log.debug("Serialized request JSON length: {}", requestJson.length());
            
            return reimbursementsServiceClient.resubmitReimbursement(
                    requestJson,
                    nullIfEmpty(serviceInvoice),
                    nullIfEmpty(itemizedBreakdown),
                    nullIfEmpty(medcert),
                    nullIfEmpty(operativeTechnique),
                    nullIfEmpty(hospitalStatement),
                    nullIfEmpty(certificateOfLiveBirth),
                    nullIfEmpty(liveBirth),
                    nullIfEmpty(doctorPrescription),
                    nullIfEmpty(irDocument),
                    nullIfEmpty(irDocumentAccredited),
                    bankDocuments
            );
        } catch (Exception e) {
            log.error("Failed to process reimbursement resubmission: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to process reimbursement resubmission: " + e.getMessage(), e);
        }
    }


    private static List<MultipartFile> nullIfEmpty(List<MultipartFile> list) {
        return (list == null || list.isEmpty()) ? null : list;
    }

    private static int sizeOf(List<?> list) {
        return list == null ? 0 : list.size();
    }
}
