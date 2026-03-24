package com.medgo.reimburse.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.medgo.commons.CommonResponse;
import com.medgo.crypto.annotation.DecryptBody;
import com.medgo.crypto.annotation.EncryptResponse;
import com.medgo.reimburse.domain.dto.ReimHistoryDTO;
import com.medgo.reimburse.domain.dto.ReimRequestDetailsRequest;
import com.medgo.reimburse.domain.dto.ReimRequestDetailsResponse;
import com.medgo.reimburse.domain.dto.ReimbursementResubmitRequest;
import com.medgo.reimburse.domain.dto.ReimbursementResubmitResponse;
import com.medgo.reimburse.domain.dto.ReimbursementSubmissionRequest;
import com.medgo.reimburse.domain.dto.ReimbursementSubmissionResponse;
import com.medgo.reimburse.domain.dto.request.DocumentUploadRequest;
import com.medgo.reimburse.service.FileValidationService;
import com.medgo.reimburse.service.ReimbursementDocumentService;
import com.medgo.reimburse.service.ReimbursementsService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import jakarta.servlet.http.HttpServletRequest;

import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/v1")
@RequiredArgsConstructor
public class ReimbursementController {
    private final ReimbursementsService reimbursementsService;
    private final FileValidationService fileValidationService;
    private final ReimbursementDocumentService documentService;
    private final ObjectMapper objectMapper;

    @GetMapping("/history")
    @EncryptResponse
    public CommonResponse memberRegistration(
            @RequestParam(name = "memberCode") String memberCode) {

        Map<String, List<ReimHistoryDTO>> reimbursementHistory =
                reimbursementsService.getReimbursementHistory(
                        memberCode);
        return CommonResponse.success(reimbursementHistory);
    }

    @PostMapping("/viewAmount")
    @EncryptResponse
    public CommonResponse getRequestDetails(
            @Valid @DecryptBody(ReimRequestDetailsRequest.class) ReimRequestDetailsRequest request) {

        ReimRequestDetailsResponse response = reimbursementsService
                .getRequestDetailsByControlCodeAndStatus(
                        request.getControlCode(),
                        request.getStatus(),
                        request.getEntryCode());
        return CommonResponse.success(response);
    }

    @PostMapping(value = "/submitReimbursement", consumes = MediaType.ALL_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.CREATED)

    // @EncryptResponse
    public CommonResponse submitReimbursement(
            @RequestPart(value = "request", required = false) String requestJson,
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
            @RequestPart(value = "bankDocuments", required = false) List<MultipartFile> bankDocuments,
            HttpServletRequest servletRequest) {

        log.info("=== Reimbursement submission request received ===");
        log.info("Incoming Content-Type: {}", servletRequest.getContentType());
        log.debug("Request JSON length: {}", requestJson != null ? requestJson.length() : 0);

        // Validate all file sections
        fileValidationService.validateFiles(serviceInvoice, "Service Invoice");
        fileValidationService.validateFiles(itemizedBreakdown, "Itemized Breakdown");
        fileValidationService.validateFiles(medcert, "Medcert");
        fileValidationService.validateFiles(operativeTechnique, "Operative Technique");
        fileValidationService.validateFiles(hospitalStatement, "Hospital Statement");
        fileValidationService.validateFiles(certificateOfLiveBirth, "Certificate of Live Birth");
        fileValidationService.validateFiles(liveBirth, "Live Birth");
        fileValidationService.validateFiles(doctorPrescription, "Doctor Prescription");
        fileValidationService.validateFiles(irDocument, "IR Document");
        fileValidationService.validateFiles(irDocumentAccredited, "IR Document Accredited");
        fileValidationService.validateSingleFileOnly(bankDocuments, "Bank Documents");

        try {
            ReimbursementSubmissionRequest request;

            String contentType = servletRequest.getContentType();
            boolean isJson = contentType != null && contentType.toLowerCase().contains(MediaType.APPLICATION_JSON_VALUE);

            if (isJson) {
                // Request sent as application/json; parse body into request DTO
                request = objectMapper.readValue(servletRequest.getInputStream(), ReimbursementSubmissionRequest.class);
            } else {
                // Expect multipart/form-data with a 'request' part containing JSON
                request = objectMapper.readValue(requestJson, ReimbursementSubmissionRequest.class);
            }
            log.info("Successfully parsed request JSON for member: {}, serviceType: {}",
                    request.getMemberCode(), request.getServiceType());

            // Validate date fields based on service type
            reimbursementsService.validateDateFields(request.getServiceType(), request.getConfinementDate(),
                    request.getDischargeDate(), request.getAvailmentDate());

            List<DocumentUploadRequest> documentUploads = documentService.buildDocumentUploads(
                    request.getServiceType(),
                    request.getNatureOfClaim(),
                    serviceInvoice,
                    itemizedBreakdown,
                    medcert,
                    operativeTechnique,
                    hospitalStatement,
                    certificateOfLiveBirth,
                    liveBirth,
                    doctorPrescription,
                    irDocument,
                    irDocumentAccredited,
                    bankDocuments
            );

            log.info("Collected {} documents for {} service type", documentUploads.size(), request.getServiceType());

            ReimbursementSubmissionResponse response = reimbursementsService.submitReimbursement(request, documentUploads);
            log.info("Reimbursement submission successful: {}", response);
            return CommonResponse.success(response);
        } catch (com.fasterxml.jackson.core.JsonProcessingException e) {
            log.error("Failed to parse request JSON: {}", e.getMessage(), e);
            throw new IllegalArgumentException("Invalid JSON format in request: " + e.getMessage(), e);
        } catch (IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            log.error("Unexpected error in submitReimbursement: {}", e.getMessage(), e);
            throw new RuntimeException("An unexpected error occurred while processing your reimbursement request: " + e.getMessage(), e);
        }
    }

    // submitReimbursement handles both multipart/form-data and application/json

    @PostMapping(value = "/resubmitReimbursement", consumes = MediaType.ALL_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK)
    public CommonResponse resubmitReimbursement(
            @RequestPart(value = "request", required = false) String requestJson,
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
            @RequestPart(value = "bankDocuments", required = false) List<MultipartFile> bankDocuments,
            HttpServletRequest servletRequest) {

        log.info("=== Reimbursement resubmission request received ===");
        log.debug("Request JSON length: {}", requestJson != null ? requestJson.length() : 0);

        // Validate all file sections
        fileValidationService.validateFiles(serviceInvoice, "Service Invoice");
        fileValidationService.validateFiles(itemizedBreakdown, "Itemized Breakdown");
        fileValidationService.validateFiles(medcert, "Medcert");
        fileValidationService.validateFiles(operativeTechnique, "Operative Technique");
        fileValidationService.validateFiles(hospitalStatement, "Hospital Statement");
        fileValidationService.validateFiles(certificateOfLiveBirth, "Certificate of Live Birth");
        fileValidationService.validateFiles(liveBirth, "Live Birth");
        fileValidationService.validateFiles(doctorPrescription, "Doctor Prescription");
        fileValidationService.validateFiles(irDocument, "IR Document");
        fileValidationService.validateFiles(irDocumentAccredited, "IR Document Accredited");
        fileValidationService.validateSingleFileOnly(bankDocuments, "Bank Documents");

        try {
            ReimbursementResubmitRequest request;

            String contentType = servletRequest.getContentType();
            boolean isJson = contentType != null && contentType.toLowerCase().contains(MediaType.APPLICATION_JSON_VALUE);

            if (isJson) {
                request = objectMapper.readValue(servletRequest.getInputStream(), ReimbursementResubmitRequest.class);
            } else {
                request = objectMapper.readValue(requestJson, ReimbursementResubmitRequest.class);
            }

            log.info("Successfully parsed resubmit request JSON for control code: {}, member: {}",
                    request.getControlCode(), request.getMemberCode());

            if (request.getControlCode() == null || request.getControlCode().isBlank()) {
                throw new IllegalArgumentException("Control code is required for resubmission");
            }

            // Validate date fields based on service type
            reimbursementsService.validateDateFields(request.getServiceType(), request.getConfinementDate(),
                    request.getDischargeDate(), request.getAvailmentDate());

            List<DocumentUploadRequest> documentUploads = documentService.buildDocumentUploads(
                    request.getServiceType(),
                    request.getNatureOfClaim(),
                    serviceInvoice,
                    itemizedBreakdown,
                    medcert,
                    operativeTechnique,
                    hospitalStatement,
                    certificateOfLiveBirth,
                    liveBirth,
                    doctorPrescription,
                    irDocument,
                    irDocumentAccredited,
                    bankDocuments
            );

            log.info("Collected {} documents for {} service type", documentUploads.size(), request.getServiceType());

            ReimbursementResubmitResponse response = reimbursementsService.resubmitReimbursement(request, documentUploads);
            log.info("Reimbursement resubmission successful: {}", response);
            return CommonResponse.success(response);
        } catch (com.fasterxml.jackson.core.JsonProcessingException e) {
            log.error("Failed to parse request JSON: {}", e.getMessage(), e);
            throw new IllegalArgumentException("Invalid JSON format in request: " + e.getMessage(), e);
        } catch (IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            log.error("Unexpected error in resubmitReimbursement: {}", e.getMessage(), e);
            throw new RuntimeException("An unexpected error occurred while processing your resubmission request: " + e.getMessage(), e);
        }
    }


    @GetMapping(value = "/images/reimbursement-success.jpg", produces = MediaType.IMAGE_JPEG_VALUE)
    public ResponseEntity<Resource> getReimbursementSuccessImage() {
        try {
            Resource resource = new ClassPathResource("images/reimbursement-success.jpg");
            if (!resource.exists()) {
                log.warn("Reimbursement success image not found in resources");
                return ResponseEntity.notFound().build();
            }

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.IMAGE_JPEG);
            headers.setCacheControl("public, max-age=31536000"); // Cache for 1 year

            return ResponseEntity.ok()
                    .headers(headers)
                    .body(resource);
        } catch (Exception e) {
            log.error("Error serving reimbursement success image: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
