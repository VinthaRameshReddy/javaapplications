package com.medgo.reimburse.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.medgo.enums.CustomStatusCode;
import com.medgo.exception.CustomException;
import com.medgo.reimburse.domain.dto.*;
import com.medgo.reimburse.domain.dto.request.DocumentUploadRequest;
import com.medgo.reimburse.domain.entity.medigo.ReimbursementDocumentEntity;
import com.medgo.reimburse.domain.entity.medigo.ReimbursementRequestEntity;
import com.medgo.reimburse.domain.entity.reimb.ReimHistory;
import com.medgo.reimburse.domain.entity.reimb.ReimVWMedgo2RequestDetails;
import com.medgo.reimburse.domain.mapper.ReimHistoryMapper;
import com.medgo.reimburse.exception.DualDatabaseTransactionException;
import com.medgo.reimburse.feign.FileManagementServiceClient;
import com.medgo.reimburse.feign.MembershipServiceClient;
import com.medgo.reimburse.repository.medigo.ReimbursementDocumentRepository;
import com.medgo.reimburse.repository.medigo.ReimbursementRequestRepository;
import com.medgo.reimburse.repository.reimb.ReimHistoryViewRepository;
import com.medgo.reimburse.service.BankAccountValidationService;
import com.medgo.reimburse.service.ReimbursementDocumentService;
import com.medgo.reimburse.service.ReimbursementNotificationService;
import com.medgo.reimburse.service.ReimbursementsService;
import com.medgo.reimburse.service.ReimDbService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.DefaultTransactionDefinition;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReimbursementsServiceImpl implements ReimbursementsService {

    private final ReimHistoryViewRepository viewRepository;
    private final ReimHistoryMapper reimHistoryMapper;
    private final ReimbursementRequestRepository reimbursementRequestRepository;
    private final ReimbursementDocumentRepository reimbursementDocumentRepository;
    private final FileManagementServiceClient fileManagementServiceClient;
    private final MembershipServiceClient membershipServiceClient;
    private final ReimbursementDocumentService documentService;
    private final ReimDbService reimDbService;
    private final BankAccountValidationService bankAccountValidationService;
    private final ReimbursementNotificationService reimbursementNotificationService;
    private final ObjectMapper objectMapper;
    @Qualifier("reimbTransactionManager")
    private final PlatformTransactionManager reimTransactionManager;

    @Override
    public Map<String, List<ReimHistoryDTO>> getReimbursementHistory(String memberCode) {
        log.info("Fetching reimbursement history for memberCode: {}", memberCode);

        List<Object[]> rows = viewRepository.findByMemberCode(memberCode);

        log.info("Found {} rows for memberCode {}", rows != null ? rows.size() : 0, memberCode);

        if (rows == null || rows.isEmpty()) {
            throw new CustomException(
                    CustomStatusCode.NO_REIMBURSEMENT_HISTORY_FOUND.getCode(),
                    CustomStatusCode.NO_REIMBURSEMENT_HISTORY_FOUND.getMessage()
            );
        }

        List<ReimHistoryDTO> list = rows.stream()
                .map(this::mapRowToEntityAndDto)
                .toList();

        return Map.of("reimbursementHistoryList", list);
    }

    @Override
    public ReimRequestDetailsResponse getRequestDetailsByControlCodeAndStatus(String controlCode, String status, String entryCode) {
        log.info("Fetching request details for controlCode: {}, status: {}, entryCode: {}", controlCode, status, entryCode);

        List<Object[]> rows;
        
        List<Object[]> initialRows = viewRepository.findByControlCodeAndStatus(controlCode, status);
        
        if (initialRows == null || initialRows.isEmpty()) {
            throw new CustomException(
                    CustomStatusCode.NO_REIMBURSEMENT_HISTORY_FOUND.getCode(),
                    "No reimbursement request found for control code '" + controlCode + "' with status '" + status + "'"
            );
        }
        
        String serviceType = safeString(initialRows.get(0)[0]);
        
        if ("Outpatient".equalsIgnoreCase(serviceType) && entryCode != null && !entryCode.isBlank()) {
            rows = viewRepository.findByControlCodeAndStatusAndEntryCode(controlCode, status, entryCode);
            
            if (rows == null || rows.isEmpty()) {
                throw new CustomException(
                        CustomStatusCode.NO_REIMBURSEMENT_HISTORY_FOUND.getCode(),
                        "No reimbursement request found for control code '" + controlCode + 
                        "', status '" + status + "', and entry code '" + entryCode + "'"
                );
            }
        } else {
            rows = initialRows;
        }

        Object[] row = rows.get(0);
        
        ReimVWMedgo2RequestDetails more = new ReimVWMedgo2RequestDetails();
        more.setServiceType(safeString(row[0]));
        more.setCheckNo(safeString(row[1]));
        more.setControlCode(safeString(row[2]));
        more.setEntryCode(safeString(row[3]));
        more.setMemberCode(safeString(row[4]));
        more.setMemLname(safeString(row[5]));
        more.setMemFname(safeString(row[6]));
        more.setMemMname(safeString(row[7]));
        more.setHoldRemarks(safeString(row[8]));
        more.setLapsePaymentRemarks(safeString(row[9]));
        more.setForRmdApproval(safeString(row[10]));
        more.setRequestedAmount(safeBigDecimal(row[11]));
        more.setApprovedAmount(safeBigDecimal(row[12]));
        more.setAvailmentDate(toLocalDateTime(row[13]));
        more.setReceivedDate(toLocalDateTime(row[14]));
        more.setEntryDate(toLocalDateTime(row[15]));
        more.setClaimNature(safeString(row[16]));
        more.setActionMemoRemarks(safeString(row[17]));
        more.setDeniedMemoRemarks(safeString(row[18]));
        more.setWhbMemoRemarks(safeString(row[19]));
        more.setActionMemo(toBoolean(row[20]));
        more.setDeniedMemo(toBoolean(row[21]));
        more.setWhbMemo(toBoolean(row[22]));

        ReimHistory hist = new ReimHistory();
        hist.setStatus(safeString(row[23]));
        hist.setMoreDetails(more);

        return reimHistoryMapper.toRequestDetailsResponse(hist, status, null);
    }

    private ReimHistoryDTO mapRowToEntityAndDto(Object[] r) {
        try {
            ReimVWMedgo2RequestDetails more = new ReimVWMedgo2RequestDetails();
            more.setServiceType(safeString(r[0]));                // service_type
            more.setCheckNo(safeString(r[1]));                    // check_no
            more.setControlCode(safeString(r[2]));                // control_code
            more.setEntryCode(safeString(r[3]));                  // entry_code
            more.setMemberCode(safeString(r[4]));                 // member_code
            more.setMemLname(safeString(r[5]));                   // mem_lname
            more.setMemFname(safeString(r[6]));                   // mem_fname
            more.setMemMname(safeString(r[7]));                   // mem_mname
            more.setHoldRemarks(safeString(r[8]));                // hold_remarks
            more.setLapsePaymentRemarks(safeString(r[9]));        // lapse_payment_remarks
            more.setForRmdApproval(safeString(r[10]));            // for_rmd_approval
            more.setRequestedAmount(safeBigDecimal(r[11]));       // requested_amount
            more.setApprovedAmount(safeBigDecimal(r[12]));        // approved_amount
            more.setAvailmentDate(toLocalDateTime(r[13]));        // availment_date
            more.setReceivedDate(toLocalDateTime(r[14]));         // received_date
            more.setEntryDate(toLocalDateTime(r[15]));            // entry_date
            more.setClaimNature(safeString(r[16]));               // claim_nature
            more.setActionMemoRemarks(safeString(r[17]));         // action_memo_remarks
            more.setDeniedMemoRemarks(safeString(r[18]));         // denied_memo_remarks
            more.setWhbMemoRemarks(safeString(r[19]));            // whb_memo_remarks
            more.setActionMemo(toBoolean(r[20]));                 // action_memo
            more.setDeniedMemo(toBoolean(r[21]));                 // denied_memo
            more.setWhbMemo(toBoolean(r[22]));                    // whb_memo

            ReimHistory hist = new ReimHistory();
            hist.setStatus(safeString(r[23]));                    // reimb_status
            hist.setMoreDetails(more);

            return reimHistoryMapper.toDTO(hist);
        } catch (Exception e) {
            log.error("Error mapping row to DTO: {}", e.getMessage(), e);
            throw new CustomException(
                    500,
                    "Error processing reimbursement history record: " + e.getMessage()
            );
        }
    }

    private String safeString(Object v) {
        if (v == null) return null;
        if (v instanceof String s) return s;
        if (v instanceof Integer i) return i.toString();
        if (v instanceof Long l) return l.toString();
        if (v instanceof Boolean b) return b.toString();
        return v.toString();
    }

    private BigDecimal safeBigDecimal(Object v) {
        if (v == null) return null;
        if (v instanceof BigDecimal bd) return bd;
        if (v instanceof Number n) return BigDecimal.valueOf(n.doubleValue());
        if (v instanceof String s) {
            try {
                return new BigDecimal(s);
            } catch (NumberFormatException e) {
                log.warn("Could not parse '{}' as BigDecimal", s);
                return null;
            }
        }
        return null;
    }

    private LocalDateTime toLocalDateTime(Object v) {
        if (v == null) return null;
        if (v instanceof LocalDateTime ldt) return ldt;
        if (v instanceof Timestamp ts) return ts.toLocalDateTime();
        if (v instanceof java.util.Date d) {
            return new Timestamp(d.getTime()).toLocalDateTime();
        }
        return null;
    }

    private Boolean toBoolean(Object v) {
        if (v == null) return null;
        if (v instanceof Boolean b) return b;
        if (v instanceof Number n) return n.intValue() != 0;
        if (v instanceof String s) {
            return "1".equals(s) || "true".equalsIgnoreCase(s) || "Y".equalsIgnoreCase(s);
        }
        return false;
    }

    @Override
    @Transactional
    public ReimbursementSubmissionResponse submitReimbursement(
            ReimbursementSubmissionRequest request,
            List<DocumentUploadRequest> documentUploads) {

        documentService.validateRequiredDocuments(
                request.getServiceType(),
                request.getNatureOfClaim(),
                documentUploads != null ? documentUploads : List.of()
        );

        if (request.getBankName() != null && request.getBankAccountNumber() != null) {
            bankAccountValidationService.validateBankAccount(
                    request.getBankName(),
                    request.getBankAccountNumber()
            );
        }

        if (request.getControlCode() != null && !request.getControlCode().isBlank()) {
            log.debug("Control code provided in request ({}) will be ignored - generating new control code", 
                    request.getControlCode());
        }
        String controlCode = generateControlCode(request.getServiceType());
        String entryCode = generateEntryCode();
        log.info("Generated new control code: {} and entry code: {} for member: {}", 
                controlCode, entryCode, request.getMemberCode());

        MemberData memberData = fetchMemberDataFromService(request.getMemberCode());
        Integer particularsCode = reimDbService.getParticularsCode(request.getNatureOfClaim());
        Float memAge = memberData.getAge();

        ReimbursementRequestEntity reimbursementRequest = ReimbursementRequestEntity.builder()
                .controlCode(controlCode)
                .entryCode(entryCode)
                .principalCode(memberData.getPrincipalCode())
                .companyCode(memberData.getCompanyCode())
                .memType(memberData.getMemType())
                .memAge(memAge)
                .costplusCode(null)
                .particularsCode(particularsCode)
                .memberCode(request.getMemberCode())
                .patientName(request.getPatientName())
                .serviceType(request.getServiceType())
                .natureOfClaim(request.getNatureOfClaim())
                .hospitalClinicName(request.getHospitalClinicName())
                .hospitalClinicAddress(request.getHospitalClinicAddress())
                .hospitalClinicPhone(request.getHospitalClinicPhone())
                .requestingDoctor(request.getRequestingDoctor())
                .confinementDate(request.getConfinementDate())
                .dischargeDate(request.getDischargeDate())
                .availmentDate(request.getAvailmentDate())
                .dateOfTreatment(request.getDateOfTreatment() != null 
                        ? request.getDateOfTreatment() 
                        : request.getAvailmentDate())
                .totalClaimAmount(request.getTotalClaimAmount())
                .disbursementMethod(request.getDisbursementMethod())
                .bankName(request.getBankName())
                .bankBranch(request.getBankBranch())
                .fullAccountName(request.getFullAccountName())
                .bankAccountNumber(request.getBankAccountNumber())
                .status("In-Process")
                .requestDate(LocalDateTime.now())
                .createdAt(LocalDateTime.now())
                .build();

        TransactionStatus reimTransaction = null;
        boolean reimDbSaved = false;
        try {
            DefaultTransactionDefinition reimTxDef = new DefaultTransactionDefinition();
            reimTxDef.setPropagationBehavior(DefaultTransactionDefinition.PROPAGATION_REQUIRES_NEW);
            reimTransaction = reimTransactionManager.getTransaction(reimTxDef);

            try {
                if ("Inpatient".equalsIgnoreCase(request.getServiceType())) {
                    reimDbService.saveIpReceiving(controlCode, entryCode, request);
                } else {
                    reimDbService.saveOpReceiving(controlCode, entryCode, request);
                }
                reimDbSaved = true;

                if (reimTransaction.isRollbackOnly()) {
                    throw new RuntimeException("ReimDB save operation failed - transaction marked for rollback");
                }
            } catch (Exception reimEx) {
                log.error("Exception during ReimDB save operation - Control Code: {}, Entry Code: {}", 
                        controlCode, entryCode, reimEx);
                throw reimEx;
            }

            reimbursementRequest = reimbursementRequestRepository.save(reimbursementRequest);
            log.info("Saved reimbursement request to Medigo DB - ID: {}, Control Code: {}, Entry Code: {}, Member: {}", 
                    reimbursementRequest.getId(), controlCode, entryCode, request.getMemberCode());

            List<ReimbursementDocumentEntity> documents = new ArrayList<>();
            if (documentUploads != null && !documentUploads.isEmpty()) {
                log.info("Processing {} documents for control code: {}", documentUploads.size(), controlCode);
                String folderName = buildFolderName(controlCode, request.getPatientName(), memberData.getAccountName());
                log.info("Using folder name for blob storage: {}", folderName);
                for (DocumentUploadRequest docUpload : documentUploads) {
                    String docType = docUpload.getDocumentType();
                    log.debug("Processing document: {} - File: {}, IsEmpty: {}", 
                            docType, 
                            docUpload.getFile() != null ? docUpload.getFile().getOriginalFilename() : "null",
                            docUpload.getFile() != null ? docUpload.getFile().isEmpty() : "N/A");
                    
                    if (docUpload.getFile() != null && !docUpload.getFile().isEmpty()) {
                        FileUploadResponse uploadResponse = fileManagementServiceClient.uploadFile(
                                docUpload.getFile(),
                                controlCode,
                                docUpload.getDocumentType(),
                                folderName
                        );

                        ReimbursementDocumentEntity document = ReimbursementDocumentEntity.builder()
                                .reimbursementRequest(reimbursementRequest)
                                .documentType(docUpload.getDocumentType())
                                .fileName(uploadResponse.getFileName())
                                .fileSize(uploadResponse.getFileSize())
                                .blobUrl(uploadResponse.getBlobUrl())
                                .blobName(uploadResponse.getBlobName())
                                .contentType(uploadResponse.getContentType())
                                .createdAt(LocalDateTime.now())
                                .build();

                        documents.add(document);
                        log.info("Uploaded document: {} ({} bytes) for control code: {}, blob: {}", 
                                docUpload.getDocumentType(), uploadResponse.getFileSize(), controlCode, uploadResponse.getBlobName());
                    } else {
                        log.warn("Skipping document {} - file is null or empty", docType);
                    }
                }
            }
            
            log.info("Total documents processed: {}, documents to save: {}", documentUploads.size(), documents.size());

            if (!documents.isEmpty()) {
                reimbursementDocumentRepository.saveAll(documents);
                log.info("Saved {} documents to database for reimbursement ID: {}, control code: {}", 
                        documents.size(), reimbursementRequest.getId(), controlCode);
            }

            if (reimTransaction.isRollbackOnly()) {
                log.error("ReimDB transaction is marked for rollback - cannot commit. Control Code: {}, Entry Code: {}", 
                        controlCode, entryCode);
                log.error("This usually indicates an error occurred during ReimDB save operations");
                throw new RuntimeException("ReimDB transaction failed and was marked for rollback");
            }
            
            try {
                reimTransactionManager.commit(reimTransaction);
                reimTransaction = null;
            } catch (org.springframework.transaction.UnexpectedRollbackException e) {
                log.error("Failed to commit ReimDB transaction - Control Code: {}, Entry Code: {}", 
                        controlCode, entryCode, e);
                throw new RuntimeException("ReimDB transaction was marked for rollback and cannot be committed: " + e.getMessage(), e);
            }

        } catch (Exception e) {
            log.error("Failed to save reimbursement - Control Code: {}, Entry Code: {}", controlCode, entryCode, e);
            
            if (reimTransaction != null && !reimTransaction.isCompleted()) {
                try {
                    reimTransactionManager.rollback(reimTransaction);
                } catch (Exception rollbackEx) {
                    log.error("Error rolling back ReimDB transaction for control code: {}", controlCode, rollbackEx);
                }
            }

            if (e instanceof DualDatabaseTransactionException) {
                throw e;
            }

            String failedDb = reimDbSaved ? "Medigo DB" : "Reim DB";
            throw new DualDatabaseTransactionException(
                    "Request submission has failed. Please try again after a few minutes.",
                    controlCode,
                    failedDb,
                    e
            );
        }

        ReimbursementRequestEntity verifiedRequest = reimbursementRequestRepository
                .findById(reimbursementRequest.getId())
                .orElse(null);
        
        if (verifiedRequest == null) {
            log.error("Reimbursement request not found in database after save - ID: {}, Control Code: {}. " +
                    "This indicates the save operation may have failed. Email notification will be skipped.", 
                    reimbursementRequest.getId(), controlCode);
            throw new RuntimeException("Reimbursement request verification failed after save. Control Code: " + controlCode);
        }

        if (!controlCode.equals(verifiedRequest.getControlCode())) {
            log.error("Control code mismatch after save - Expected: {}, Found: {}. ID: {}", 
                    controlCode, verifiedRequest.getControlCode(), verifiedRequest.getId());
            throw new RuntimeException("Control code mismatch after save operation. Control Code: " + controlCode);
        }

        List<ReimbursementDocumentEntity> savedDocuments = 
                reimbursementDocumentRepository.findByReimbursementRequestId(verifiedRequest.getId());

        ReimbursementSubmissionResponse response = ReimbursementSubmissionResponse.builder()
                .id(verifiedRequest.getId())
                .controlCode(controlCode)
                .status(verifiedRequest.getStatus())
                .requestDate(verifiedRequest.getRequestDate())
                .dateOfTreatment(verifiedRequest.getDateOfTreatment())
                .totalClaimAmount(verifiedRequest.getTotalClaimAmount())
                .patientName(verifiedRequest.getPatientName())
                .memberCode(verifiedRequest.getMemberCode())
                .serviceType(verifiedRequest.getServiceType())
                .natureOfClaim(verifiedRequest.getNatureOfClaim())
                .disbursementMethod(verifiedRequest.getDisbursementMethod())
                .bankName(verifiedRequest.getBankName())
                .bankAccountNumber(verifiedRequest.getBankAccountNumber())
                .fullAccountName(verifiedRequest.getFullAccountName())
                .documents(savedDocuments.stream()
                        .map(doc -> DocumentInfo.builder()
                                .id(doc.getId())
                                .documentType(doc.getDocumentType())
                                .fileName(doc.getFileName())
                                .blobUrl(doc.getBlobUrl())
                                .fileSize(doc.getFileSize())
                                .build())
                        .collect(Collectors.toList()))
                .build();

        String deviceId = null;
        String applicationId = null;
        String userEmail = null;
        try {
            org.springframework.web.context.request.ServletRequestAttributes attrs = 
                (org.springframework.web.context.request.ServletRequestAttributes) 
                org.springframework.web.context.request.RequestContextHolder.getRequestAttributes();
            if (attrs != null) {
                jakarta.servlet.http.HttpServletRequest req = attrs.getRequest();
                if (req != null) {
                    deviceId = req.getHeader("X-DEVICE-ID");
                    applicationId = req.getHeader("X-APPLICATION-ID");

                    String userIdHeader = req.getHeader("userId");
                    if (userIdHeader != null && !userIdHeader.isBlank() && 
                        userIdHeader.contains("@") && userIdHeader.contains(".") && userIdHeader.length() > 5) {
                        userEmail = userIdHeader;
                    } else {
                        String bearerToken = req.getHeader("Authorization");
                        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
                            String token = bearerToken.substring(7);
                            try {
                                String[] parts = token.split("\\.");
                                if (parts.length == 3) {
                                    String payload = parts[1];
                                    while (payload.length() % 4 != 0) {
                                        payload += "=";
                                    }
                                    byte[] decodedBytes = java.util.Base64.getUrlDecoder().decode(payload);
                                    String payloadJson = new String(decodedBytes);
                                    
                                    String[] emailClaims = {"email", "username", "preferred_username"};
                                    for (String claim : emailClaims) {
                                        String claimPattern = "\"" + claim + "\"";
                                        if (payloadJson.contains(claimPattern)) {
                                            int claimStart = payloadJson.indexOf(claimPattern) + claimPattern.length();
                                            while (claimStart < payloadJson.length() && 
                                                   (payloadJson.charAt(claimStart) == ':' || 
                                                    payloadJson.charAt(claimStart) == ' ' || 
                                                    payloadJson.charAt(claimStart) == '"')) {
                                                claimStart++;
                                            }
                                            int claimEnd = claimStart;
                                            while (claimEnd < payloadJson.length() && 
                                                   payloadJson.charAt(claimEnd) != '"' && 
                                                   payloadJson.charAt(claimEnd) != ',' &&
                                                   payloadJson.charAt(claimEnd) != '}') {
                                                claimEnd++;
                                            }
                                            if (claimEnd > claimStart) {
                                                String claimValue = payloadJson.substring(claimStart, claimEnd);
                                                if (claimValue.contains("@") && claimValue.contains(".") && claimValue.length() > 5) {
                                                    userEmail = claimValue;
                                                    break;
                                                }
                                            }
                                        }
                                    }
                                }
                            } catch (Exception tokenEx) {

                            }
                        }
                    }

                    if (deviceId != null && !deviceId.isBlank()) {
                        com.medgo.commons.RequestContext.setDeviceId(deviceId);
                    }
                }
            }
        } catch (Exception e) {
            log.warn("Could not extract headers from request context: {}", e.getMessage());
        }
        
        final String finalDeviceId = deviceId;
        final String finalApplicationId = applicationId;
        final String finalUserEmail = userEmail;

        log.info("Sending email notification for successfully submitted reimbursement - Control Code: {} (submitReimbursement)", controlCode);
        try {
            reimbursementRequestRepository.flush(); // ensure inserted row is visible to the atomic update below
            int updated = reimbursementRequestRepository.markSubmissionEmailSentIfNotSent(controlCode, LocalDateTime.now());
            if (updated == 1) {
                reimbursementNotificationService.sendControlCodeEmail(response, finalDeviceId, finalApplicationId, finalUserEmail);
                log.info("Email notification completed for control code: {}", controlCode);
            } else if (updated == 0) {
                log.warn("Submission email already sent or duplicate request for control code: {}, skipping email to prevent duplicate", controlCode);
            } else {
                reimbursementNotificationService.sendControlCodeEmail(response, finalDeviceId, finalApplicationId, finalUserEmail);
                log.info("Email notification completed for control code: {}", controlCode);
            }
        } catch (Exception e) {
            log.warn("Deduplication check failed for control code {} (e.g. column submission_email_sent_at may not exist yet). Sending email anyway: {}", controlCode, e.getMessage());
            reimbursementNotificationService.sendControlCodeEmail(response, finalDeviceId, finalApplicationId, finalUserEmail);
            log.info("Email notification completed for control code: {}", controlCode);
        }

        return response;
    }

    @Override
    @Transactional
    public ReimbursementResubmitResponse resubmitReimbursement(
            ReimbursementResubmitRequest request,
            List<DocumentUploadRequest> newDocumentUploads) {

        ReimbursementRequestEntity existingRequest = reimbursementRequestRepository
                .findByControlCode(request.getControlCode())
                .orElseThrow(() -> new IllegalArgumentException(
                        "Reimbursement request with control code " + request.getControlCode() + " not found"));

        if ("Action Needed".equalsIgnoreCase(existingRequest.getStatus())
                || "Returned for Correction".equalsIgnoreCase(existingRequest.getStatus())) {
            throw new IllegalArgumentException(
                    "Reimbursement request with control code " + request.getControlCode() 
                    + " is not in 'Action Need' status. Current status: " + existingRequest.getStatus());
        }

        log.info("Resubmitting reimbursement request - ID: {}, Control Code: {}, Member: {}", 
                existingRequest.getId(), request.getControlCode(), request.getMemberCode());

        if (newDocumentUploads != null && !newDocumentUploads.isEmpty()) {
            documentService.validateRequiredDocuments(
                    request.getServiceType(),
                    request.getNatureOfClaim(),
                    newDocumentUploads
            );
        }

        if (request.getBankName() != null && request.getBankAccountNumber() != null) {
            bankAccountValidationService.validateBankAccount(
                    request.getBankName(),
                    request.getBankAccountNumber()
            );
        }

        MemberData memberData = fetchMemberDataFromService(request.getMemberCode());
        Integer particularsCode = reimDbService.getParticularsCode(request.getNatureOfClaim());
        Float memAge = memberData.getAge();

        existingRequest.setMemberCode(request.getMemberCode());
        existingRequest.setPrincipalCode(memberData.getPrincipalCode());
        existingRequest.setCompanyCode(memberData.getCompanyCode());
        existingRequest.setMemType(memberData.getMemType());
        existingRequest.setMemAge(memAge);
        existingRequest.setCostplusCode(null);
        existingRequest.setParticularsCode(particularsCode);
        existingRequest.setPatientName(request.getPatientName());
        existingRequest.setServiceType(request.getServiceType());
        existingRequest.setNatureOfClaim(request.getNatureOfClaim());
        existingRequest.setHospitalClinicName(request.getHospitalClinicName());
        existingRequest.setHospitalClinicAddress(request.getHospitalClinicAddress());
        existingRequest.setHospitalClinicPhone(request.getHospitalClinicPhone());
        existingRequest.setRequestingDoctor(request.getRequestingDoctor());
        existingRequest.setConfinementDate(request.getConfinementDate());
        existingRequest.setDischargeDate(request.getDischargeDate());
        existingRequest.setAvailmentDate(request.getAvailmentDate());
        existingRequest.setDateOfTreatment(request.getDateOfTreatment() != null 
                ? request.getDateOfTreatment() 
                : request.getAvailmentDate());
        existingRequest.setTotalClaimAmount(request.getTotalClaimAmount());
        existingRequest.setDisbursementMethod(request.getDisbursementMethod());
        existingRequest.setBankName(request.getBankName());
        existingRequest.setBankBranch(request.getBankBranch());
        existingRequest.setFullAccountName(request.getFullAccountName());
        existingRequest.setBankAccountNumber(request.getBankAccountNumber());
        existingRequest.setStatus("In-Process");
        existingRequest.setUpdatedAt(LocalDateTime.now());
        existingRequest.setSubmissionEmailSentAt(null); // allow one confirmation email for this resubmission (deduped atomically)

        String entryCode = existingRequest.getEntryCode();
        TransactionStatus reimTransaction = null;
        boolean reimDbUpdated = false;
        try {
            DefaultTransactionDefinition reimTxDef = new DefaultTransactionDefinition();
            reimTxDef.setPropagationBehavior(DefaultTransactionDefinition.PROPAGATION_REQUIRES_NEW);
            reimTransaction = reimTransactionManager.getTransaction(reimTxDef);

            if ("Inpatient".equalsIgnoreCase(request.getServiceType())) {
                reimDbService.updateIpReceiving(request.getControlCode(), entryCode, request);
            } else {
                reimDbService.updateOpReceiving(request.getControlCode(), entryCode, request);
            }
            reimDbUpdated = true;

            existingRequest = reimbursementRequestRepository.save(existingRequest);

            if (request.getDocumentIdsToRemove() != null && !request.getDocumentIdsToRemove().isEmpty()) {
                List<ReimbursementDocumentEntity> documentsToRemove = 
                        reimbursementDocumentRepository.findAllById(request.getDocumentIdsToRemove());

                for (ReimbursementDocumentEntity doc : documentsToRemove) {
                    if (!doc.getReimbursementRequest().getId().equals(existingRequest.getId())) {
                        throw new IllegalArgumentException(
                                "Document with ID " + doc.getId() + " does not belong to this reimbursement request");
                    }
                }
                
                reimbursementDocumentRepository.deleteAll(documentsToRemove);
            }

            List<ReimbursementDocumentEntity> newDocuments = new ArrayList<>();
            if (newDocumentUploads != null && !newDocumentUploads.isEmpty()) {
                log.info("Processing {} new documents for resubmission control code: {}", 
                        newDocumentUploads.size(), request.getControlCode());
                String folderName = buildFolderName(request.getControlCode(), request.getPatientName(), memberData.getAccountName());
                log.info("Using folder name for blob storage (resubmission): {}", folderName);
                for (DocumentUploadRequest docUpload : newDocumentUploads) {
                    String docType = docUpload.getDocumentType();
                    log.debug("Processing new document: {} - File: {}, IsEmpty: {}", 
                            docType, 
                            docUpload.getFile() != null ? docUpload.getFile().getOriginalFilename() : "null",
                            docUpload.getFile() != null ? docUpload.getFile().isEmpty() : "N/A");
                    
                    if (docUpload.getFile() != null && !docUpload.getFile().isEmpty()) {
                        FileUploadResponse uploadResponse = fileManagementServiceClient.uploadFile(
                                docUpload.getFile(),
                                request.getControlCode(),
                                docUpload.getDocumentType(),
                                folderName
                        );

                        ReimbursementDocumentEntity document = ReimbursementDocumentEntity.builder()
                                .reimbursementRequest(existingRequest)
                                .documentType(docUpload.getDocumentType())
                                .fileName(uploadResponse.getFileName())
                                .fileSize(uploadResponse.getFileSize())
                                .blobUrl(uploadResponse.getBlobUrl())
                                .blobName(uploadResponse.getBlobName())
                                .contentType(uploadResponse.getContentType())
                                .createdAt(LocalDateTime.now())
                                .build();

                        newDocuments.add(document);
                        log.info("Uploaded new document: {} ({} bytes) for control code: {}, blob: {}", 
                                docUpload.getDocumentType(), uploadResponse.getFileSize(), 
                                request.getControlCode(), uploadResponse.getBlobName());
                    } else {
                        log.warn("Skipping new document {} - file is null or empty", docType);
                    }
                }
                
                log.info("Total new documents processed: {}, documents to save: {}", 
                        newDocumentUploads.size(), newDocuments.size());
            }

            if (!newDocuments.isEmpty()) {
                reimbursementDocumentRepository.saveAll(newDocuments);
            }

            reimTransactionManager.commit(reimTransaction);
            reimTransaction = null;

        } catch (Exception e) {
            log.error("Failed to resubmit reimbursement - Control Code: {}, Entry Code: {}", 
                    request.getControlCode(), entryCode, e);

            if (reimTransaction != null && !reimTransaction.isCompleted()) {
                try {
                    reimTransactionManager.rollback(reimTransaction);
                } catch (Exception rollbackEx) {
                    log.error("Error rolling back ReimDB transaction for control code: {}", request.getControlCode(), rollbackEx);
                }
            }

            if (e instanceof DualDatabaseTransactionException) {
                throw e;
            }

            String failedDb = reimDbUpdated ? "Medigo DB" : "Reim DB";
            throw new DualDatabaseTransactionException(
                    "Request submission has failed. Please try again after a few minutes.",
                    request.getControlCode(),
                    failedDb,
                    e
            );
        }

        ReimbursementRequestEntity verifiedRequest = reimbursementRequestRepository
                .findByControlCode(request.getControlCode())
                .orElse(null);
        
        if (verifiedRequest == null) {
            log.error("Reimbursement request not found in database after resubmission - Control Code: {}. " +
                    "This indicates the update operation may have failed. Email notification will be skipped.", 
                    request.getControlCode());
            throw new RuntimeException("Reimbursement request verification failed after resubmission. Control Code: " + request.getControlCode());
        }

        if (!"In-Process".equalsIgnoreCase(verifiedRequest.getStatus())) {
            log.warn("Reimbursement status after resubmission is '{}', expected 'In-Process'. Control Code: {}", 
                    verifiedRequest.getStatus(), request.getControlCode());
        }

        List<ReimbursementDocumentEntity> allDocuments = 
                reimbursementDocumentRepository.findByReimbursementRequestId(verifiedRequest.getId());

        ReimbursementResubmitResponse response = ReimbursementResubmitResponse.builder()
                .id(verifiedRequest.getId())
                .controlCode(request.getControlCode())
                .status(verifiedRequest.getStatus())
                .requestDate(verifiedRequest.getRequestDate())
                .dateOfTreatment(verifiedRequest.getDateOfTreatment())
                .totalClaimAmount(verifiedRequest.getTotalClaimAmount())
                .patientName(verifiedRequest.getPatientName())
                .memberCode(verifiedRequest.getMemberCode())
                .serviceType(verifiedRequest.getServiceType())
                .natureOfClaim(verifiedRequest.getNatureOfClaim())
                .disbursementMethod(verifiedRequest.getDisbursementMethod())
                .bankName(verifiedRequest.getBankName())
                .bankAccountNumber(verifiedRequest.getBankAccountNumber())
                .fullAccountName(verifiedRequest.getFullAccountName())
                .documents(allDocuments.stream()
                        .map(doc -> DocumentInfo.builder()
                                .id(doc.getId())
                                .documentType(doc.getDocumentType())
                                .fileName(doc.getFileName())
                                .blobUrl(doc.getBlobUrl())
                                .fileSize(doc.getFileSize())
                                .build())
                        .collect(Collectors.toList()))
                .message("We've received your resubmitted reimbursement request and have started processing it. " +
                        "This may take up to 15 business days. Rest assured that we'll keep you updated.")
                .build();

        String deviceId = null;
        String applicationId = null;
        String userEmail = null;
        try {
            org.springframework.web.context.request.ServletRequestAttributes attrs = 
                (org.springframework.web.context.request.ServletRequestAttributes) 
                org.springframework.web.context.request.RequestContextHolder.getRequestAttributes();
            if (attrs != null) {
                jakarta.servlet.http.HttpServletRequest req = attrs.getRequest();
                if (req != null) {
                    deviceId = req.getHeader("X-DEVICE-ID");
                    applicationId = req.getHeader("X-APPLICATION-ID");

                    String userIdHeader = req.getHeader("userId");
                    if (userIdHeader != null && !userIdHeader.isBlank() && 
                        userIdHeader.contains("@") && userIdHeader.contains(".") && userIdHeader.length() > 5) {
                        userEmail = userIdHeader;
                    } else {
                        String bearerToken = req.getHeader("Authorization");
                        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
                            String token = bearerToken.substring(7);
                            try {
                                String[] parts = token.split("\\.");
                                if (parts.length == 3) {
                                    String payload = parts[1];
                                    while (payload.length() % 4 != 0) {
                                        payload += "=";
                                    }
                                    byte[] decodedBytes = java.util.Base64.getUrlDecoder().decode(payload);
                                    String payloadJson = new String(decodedBytes);
                                    
                                    String[] emailClaims = {"email", "username", "preferred_username"};
                                    for (String claim : emailClaims) {
                                        String claimPattern = "\"" + claim + "\"";
                                        if (payloadJson.contains(claimPattern)) {
                                            int claimStart = payloadJson.indexOf(claimPattern) + claimPattern.length();
                                            while (claimStart < payloadJson.length() && 
                                                   (payloadJson.charAt(claimStart) == ':' || 
                                                    payloadJson.charAt(claimStart) == ' ' || 
                                                    payloadJson.charAt(claimStart) == '"')) {
                                                claimStart++;
                                            }
                                            int claimEnd = claimStart;
                                            while (claimEnd < payloadJson.length() && 
                                                   payloadJson.charAt(claimEnd) != '"' && 
                                                   payloadJson.charAt(claimEnd) != ',' &&
                                                   payloadJson.charAt(claimEnd) != '}') {
                                                claimEnd++;
                                            }
                                            if (claimEnd > claimStart) {
                                                String claimValue = payloadJson.substring(claimStart, claimEnd);
                                                if (claimValue.contains("@") && claimValue.contains(".") && claimValue.length() > 5) {
                                                    userEmail = claimValue;
                                                    break;
                                                }
                                            }
                                        }
                                    }
                                }
                            } catch (Exception tokenEx) {
                                log.debug("Could not extract email from JWT token: {}", tokenEx.getMessage());
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            log.warn("Could not extract headers from request context: {}", e.getMessage());
        }
        
        final String finalDeviceId = deviceId;
        final String finalApplicationId = applicationId;
        final String finalUserEmail = userEmail;

        ReimbursementSubmissionResponse notificationResponse = ReimbursementSubmissionResponse.builder()
                .id(response.getId())
                .controlCode(response.getControlCode())
                .status(response.getStatus())
                .requestDate(response.getRequestDate())
                .dateOfTreatment(response.getDateOfTreatment())
                .totalClaimAmount(response.getTotalClaimAmount())
                .patientName(response.getPatientName())
                .memberCode(response.getMemberCode())
                .serviceType(response.getServiceType())
                .natureOfClaim(response.getNatureOfClaim())
                .disbursementMethod(response.getDisbursementMethod())
                .bankName(response.getBankName())
                .bankAccountNumber(response.getBankAccountNumber())
                .fullAccountName(response.getFullAccountName())
                .build();

        log.info("Sending email notification for successfully resubmitted reimbursement - Control Code: {} (resubmitReimbursement)", request.getControlCode());
        try {
            reimbursementRequestRepository.flush(); // ensure updated row is visible to the atomic update below
            int updated = reimbursementRequestRepository.markSubmissionEmailSentIfNotSent(request.getControlCode(), LocalDateTime.now());
            if (updated == 1) {
                reimbursementNotificationService.sendControlCodeEmail(notificationResponse, finalDeviceId, finalApplicationId, finalUserEmail);
                log.info("Email notification completed for control code: {}", request.getControlCode());
            } else if (updated == 0) {
                log.warn("Resubmission email already sent or duplicate request for control code: {}, skipping email to prevent duplicate", request.getControlCode());
            } else {
                reimbursementNotificationService.sendControlCodeEmail(notificationResponse, finalDeviceId, finalApplicationId, finalUserEmail);
                log.info("Email notification completed for control code: {}", request.getControlCode());
            }
        } catch (Exception e) {
            log.warn("Deduplication check failed for control code {} (e.g. column submission_email_sent_at may not exist yet). Sending email anyway: {}", request.getControlCode(), e.getMessage());
            reimbursementNotificationService.sendControlCodeEmail(notificationResponse, finalDeviceId, finalApplicationId, finalUserEmail);
            log.info("Email notification completed for control code: {}", request.getControlCode());
        }

        return response;
    }

    private String generateControlCode(String serviceType) {
        String prefix = serviceType != null && serviceType.equalsIgnoreCase("Inpatient") ? "IP" : "OP";
        String codePrefix = "REMG-" + prefix;

        long maxSequenceMedigo = reimbursementRequestRepository.findAll().stream()
                .filter(r -> r.getControlCode() != null && r.getControlCode().startsWith(codePrefix))
                .mapToLong(r -> {
                    try {
                        String code = r.getControlCode();
                        if (code.length() > codePrefix.length()) {
                            String seqPart = code.substring(codePrefix.length());
                            return Long.parseLong(seqPart);
                        }
                        return 0;
                    } catch (NumberFormatException e) {
                        return 0;
                    }
                })
                .max()
                .orElse(0);

        long maxSequenceReim = reimDbService.getMaxControlCodeSequence(codePrefix);
        
        long maxSequence = Math.max(maxSequenceMedigo, maxSequenceReim);
        
        String sequence = String.format("%06d", maxSequence + 1);
        return codePrefix + sequence;
    }

    private String generateEntryCode() {
        String codePrefix = "RMGOP";
        long maxSequenceMedigo = reimbursementRequestRepository.findAll().stream()
                .filter(r -> r.getEntryCode() != null && r.getEntryCode().startsWith(codePrefix))
                .mapToLong(r -> {
                    try {
                        String code = r.getEntryCode();
                        if (code.length() > codePrefix.length()) {
                            String seqPart = code.substring(codePrefix.length());
                            return Long.parseLong(seqPart);
                        }
                        return 0;
                    } catch (NumberFormatException e) {
                        return 0;
                    }
                })
                .max()
                .orElse(0);

        long maxSequenceReim = reimDbService.getMaxEntryCodeSequence(codePrefix);
        
        long maxSequence = Math.max(maxSequenceMedigo, maxSequenceReim);
        
        String sequence = String.format("%06d", maxSequence + 1);
        return codePrefix + sequence;
    }

    private NameParts parsePatientName(String patientName) {
        if (patientName == null || patientName.trim().isEmpty()) {
            return new NameParts("", "");
        }

        String trimmed = patientName.trim();
        String[] parts = trimmed.split("\\s+");

        if (parts.length == 1) {
            return new NameParts(parts[0], "");
        } else if (parts.length == 2) {
            return new NameParts(parts[0], parts[1]);
        } else {
            String firstName = parts[0];
            String lastName = parts[parts.length - 1];
            return new NameParts(firstName, lastName);
        }
    }

    private String sanitizeForFolderName(String name) {
        if (name == null || name.trim().isEmpty()) {
            return "";
        }
        return name.trim()
                .replaceAll("\\s+", "_")
                .replaceAll("[^a-zA-Z0-9_]", "")
                .toUpperCase();
    }
    private String buildFolderName(String controlCode, String patientName, String accountName) {
        NameParts nameParts = parsePatientName(patientName);
        String firstName = sanitizeForFolderName(nameParts.firstName);
        String lastName = sanitizeForFolderName(nameParts.lastName);
        String companyName = sanitizeForFolderName(accountName != null ? accountName : "");

        if (firstName.isEmpty()) firstName = "UNKNOWN";
        if (lastName.isEmpty()) lastName = "UNKNOWN";
        if (companyName.isEmpty()) companyName = "UNKNOWN";
        
        return String.format("%s_%s_%s_%s", 
                controlCode != null ? controlCode : "",
                firstName,
                lastName,
                companyName);
    }
    private static class NameParts {
        String firstName;
        String lastName;

        NameParts(String firstName, String lastName) {
            this.firstName = firstName != null ? firstName : "";
            this.lastName = lastName != null ? lastName : "";
        }
    }

    private MemberData fetchMemberDataFromService(String memberCode) {
        try {
            log.info("Fetching member data via Feign call to shared-membership-service for memberCode: {}", memberCode);
            Object responseObj = membershipServiceClient.findMemberByCode(memberCode, null);

            if (!(responseObj instanceof Map)) {
                log.warn("Membership service returned unexpected response type for memberCode: {}", memberCode);
                throw new CustomException(
                    CustomStatusCode.NO_REIMBURSEMENT_HISTORY_FOUND.getCode(),
                    "Member not found or invalid response from membership service"
                );
            }

            @SuppressWarnings("unchecked")
            Map<String, Object> responseMap = (Map<String, Object>) responseObj;
            Object data = responseMap.get("data");

            if (data == null) {
                log.warn("Membership service returned null data for memberCode: {}", memberCode);
                throw new CustomException(
                    CustomStatusCode.NO_REIMBURSEMENT_HISTORY_FOUND.getCode(),
                    "Member data not found for member code: " + memberCode
                );
            }

            @SuppressWarnings("unchecked")
            Map<String, Object> dataMap = (data instanceof Map)
                    ? (Map<String, Object>) data
                    : objectMapper.convertValue(data, Map.class);
            
            String principalCode = getStringValue(dataMap, "principalCode");
            String accountCode = getStringValue(dataMap, "accountCode");
            String accountName = getStringValue(dataMap, "accountName");
            String memTypeString = getStringValue(dataMap, "memType");
            
            Float age = null;
            Object ageObj = dataMap.get("age");
            if (ageObj != null) {
                try {
                    if (ageObj instanceof Number) {
                        age = ((Number) ageObj).floatValue();
                    } else if (ageObj instanceof String) {
                        String ageStr = ((String) ageObj).trim();
                        if (!ageStr.isEmpty()) {
                            age = Float.parseFloat(ageStr);
                        }
                    }
                } catch (Exception e) {
                    log.warn("Failed to parse age from membership response: {}", ageObj, e);
                }
            }
            
            LocalDate birthDate = null;
            Object birthDateObj = dataMap.get("birthDate");
            if (birthDateObj == null) {
                birthDateObj = dataMap.get("memBday");
            }
            if (birthDateObj == null) {
                birthDateObj = dataMap.get("mem_bday");
            }
            
            if (birthDateObj != null) {
                if (birthDateObj instanceof LocalDateTime) {
                    birthDate = ((LocalDateTime) birthDateObj).toLocalDate();
                } else if (birthDateObj instanceof java.sql.Timestamp) {
                    birthDate = ((java.sql.Timestamp) birthDateObj).toLocalDateTime().toLocalDate();
                } else if (birthDateObj instanceof String) {
                    try {
                        String birthDateStr = (String) birthDateObj;
                        try {
                            LocalDateTime dateTime = LocalDateTime.parse(birthDateStr);
                            birthDate = dateTime.toLocalDate();
                        } catch (Exception e) {
                            birthDate = LocalDate.parse(birthDateStr);
                        }
                    } catch (Exception e) {
                        log.warn("Failed to parse birthDate/mem_bday: {}", birthDateObj, e);
                    }
                }
            }
            
            Short memType = convertMemTypeToShort(memTypeString, memberCode, principalCode);
            
            log.info("Successfully fetched member data via Feign - principalCode: {}, companyCode: {}, accountName: {}, memType: {}, age: {}", 
                    principalCode, accountCode, accountName, memType, age);
            
            return MemberData.builder()
                    .principalCode(principalCode)
                    .companyCode(accountCode)
                    .accountName(accountName)
                    .birthDate(birthDate)
                    .age(age)
                    .memType(memType)
                    .memTypeString(memTypeString)
                    .build();
                    
        } catch (CustomException e) {
            throw e;
        } catch (Exception e) {
            log.error("Failed to fetch member data via Feign call for memberCode: {}", memberCode, e);
            throw new RuntimeException("Failed to fetch member data from membership service: " + e.getMessage(), e);
        }
    }
    private String getStringValue(Map<String, Object> map, String key) {
        Object value = map.get(key);
        return value != null ? value.toString() : null;
    }
    private Short convertMemTypeToShort(String memTypeString, String memberCode, String principalCode) {
        if (principalCode == null || principalCode.equals(memberCode)) {
            return 0;
        }
        
        if (memTypeString != null && memTypeString.toLowerCase().contains("extended")) {
            return 2;
        }
        
        return 1;
    }

    @Override
    public void validateDateFields(String serviceType, LocalDate confinementDate, 
                                     LocalDate dischargeDate, LocalDate availmentDate) {
        if (serviceType == null || serviceType.isBlank()) {
            throw new IllegalArgumentException("Service type is required");
        }

        String serviceTypeUpper = serviceType.trim().toUpperCase();
        boolean isInpatient = "INPATIENT".equals(serviceTypeUpper) || "IP".equals(serviceTypeUpper);
        boolean isOutpatient = "OUTPATIENT".equals(serviceTypeUpper) || "OP".equals(serviceTypeUpper);

        if (isInpatient) {
            if (confinementDate == null) {
                throw new IllegalArgumentException("Confinement date is required for Inpatient service type");
            }
            if (dischargeDate == null) {
                throw new IllegalArgumentException("Discharge date is required for Inpatient service type");
            }
            log.debug("Date validation passed for Inpatient: confinementDate={}, dischargeDate={}", 
                    confinementDate, dischargeDate);
        } else if (isOutpatient) {
            if (availmentDate == null) {
                throw new IllegalArgumentException("Availment date is required for Outpatient service type");
            }
            log.debug("Date validation passed for Outpatient: availmentDate={}", availmentDate);
        } else {
            throw new IllegalArgumentException("Invalid service type: " + serviceType + ". Must be 'Inpatient' or 'Outpatient'");
        }
    }
}
