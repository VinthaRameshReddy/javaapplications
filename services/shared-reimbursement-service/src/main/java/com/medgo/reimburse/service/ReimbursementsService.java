package com.medgo.reimburse.service;


import com.medgo.reimburse.domain.dto.ReimHistoryDTO;
import com.medgo.reimburse.domain.dto.ReimRequestDetailsResponse;
import com.medgo.reimburse.domain.dto.ReimbursementResubmitRequest;
import com.medgo.reimburse.domain.dto.ReimbursementResubmitResponse;
import com.medgo.reimburse.domain.dto.ReimbursementSubmissionRequest;
import com.medgo.reimburse.domain.dto.ReimbursementSubmissionResponse;
import com.medgo.reimburse.domain.dto.request.DocumentUploadRequest;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public interface ReimbursementsService {
    Map<String, List<ReimHistoryDTO>> getReimbursementHistory(String memberCode);
    
    ReimRequestDetailsResponse getRequestDetailsByControlCodeAndStatus(String controlCode, String status, String entryCode);
    
    ReimbursementSubmissionResponse submitReimbursement(
            ReimbursementSubmissionRequest request,
            List<DocumentUploadRequest> documentUploads);
    
    ReimbursementResubmitResponse resubmitReimbursement(
            ReimbursementResubmitRequest request,
            List<DocumentUploadRequest> documentUploads);
    
    void validateDateFields(String serviceType, LocalDate confinementDate, 
                           LocalDate dischargeDate, LocalDate availmentDate);
}
