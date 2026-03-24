package com.medgo.utils.service;

import com.medgo.utils.domain.MedgoEventAudit;
import com.medgo.utils.repository.MedgoEventAuditRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service for managing MedgoEventAudit entries.
 * Provides methods for saving and querying audit logs.
 * Uses REQUIRES_NEW propagation for save operations to ensure audit logs are saved
 * even if the main transaction fails or rolls back.
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class MedgoEventAuditService {

    private final MedgoEventAuditRepository medgoEventAuditRepository;

    /**
     * Saves audit log in a new transaction to ensure it's committed
     * even if the main transaction rolls back.
     * This is critical for staging/production environments where
     * transaction management is stricter.
     * 
     * Note: Spring will automatically use the transaction manager
     * associated with the repository's entity manager factory.
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void saveAuditLog(MedgoEventAudit audit) {
        try {
            medgoEventAuditRepository.saveAndFlush(audit);
            log.debug("Audit log saved successfully - EventType: {} | Username: {}", 
                    audit.getEventType(), audit.getUsername());
        } catch (Exception e) {
            log.error("Failed to save audit log - EventType: {} | Username: {} | Error: {}", 
                    audit.getEventType(), audit.getUsername(), e.getMessage(), e);
            throw e; // Re-throw to ensure transaction rollback if needed
        }
    }

}

