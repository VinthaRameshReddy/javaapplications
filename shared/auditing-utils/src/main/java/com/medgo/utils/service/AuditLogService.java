package com.medgo.utils.service;

import com.medgo.utils.domain.AuditLogEntity;
import com.medgo.utils.repository.AuditLogRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class AuditLogService {

    private final AuditLogRepository auditLogRepository;

    @Autowired
    public AuditLogService(AuditLogRepository auditLogRepository) {
        this.auditLogRepository = auditLogRepository;
    }


    public  void saveAuditLog(String loginId, String trackerId, String mobileNumber,
                                    String applicationId, String request, String response, String apiUrl, String apiName, String status) {
        AuditLogEntity auditLogEntity = new AuditLogEntity();
        auditLogEntity.setLoginId(loginId);
        auditLogEntity.setTxn_ref_num(trackerId);
        auditLogEntity.setMobileNumber(mobileNumber);
        auditLogEntity.setApplicationId(applicationId);
        auditLogEntity.setRequest(request);
        auditLogEntity.setResponse(response);
        auditLogEntity.setApiName(apiName);
        auditLogEntity.setApiUrl(apiUrl);
        auditLogEntity.setStatus(status);
        auditLogRepository.save(auditLogEntity);
    }

}
