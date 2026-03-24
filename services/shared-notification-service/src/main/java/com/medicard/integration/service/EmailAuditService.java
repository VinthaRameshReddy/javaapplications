// src/main/java/com/medicard/integration/service/EmailAuditService.java
package com.medicard.integration.service;

import com.medicard.integration.domain.entity.EmailAuditLog;
import com.medicard.integration.domain.request.EmailRequest;
import com.medicard.integration.domain.request.EmailStatus;
import com.medicard.integration.repository.EmailAuditRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailAuditService {

    private final EmailAuditRepository emailAuditRepository;

    /**
     * Helper method to get all recipients from a request.
     * Consider moving this into the EmailRequest class itself.
     */
    private List<String> getAllRecipients(EmailRequest request) {
        List<String> all = new ArrayList<>();
        if (request.getToEmails() != null) all.addAll(request.getToEmails());
        if (request.getCcEmails() != null) all.addAll(request.getCcEmails());
        if (request.getBccEmails() != null) all.addAll(request.getBccEmails());
        return all.stream().filter(Objects::nonNull).collect(Collectors.toList());
    }

    /**
     * Logs the status of an email attempt. Runs in a new transaction to ensure
     * the audit log is committed even if the calling method's transaction fails.
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void logStatus(UUID correlationId, int attempt, EmailStatus status, String details, EmailRequest request) {
        try {
            EmailAuditLog logEntry = new EmailAuditLog();
            logEntry.setCorrelationId(correlationId);
            logEntry.setAttemptNumber(attempt);
            logEntry.setStatus(status);
            logEntry.setType(request.getType());
            logEntry.setDetails(details);
            logEntry.setSubject(request.getSubject());
            logEntry.setRecipients(String.join(", ", getAllRecipients(request)));

            emailAuditRepository.save(logEntry);
        } catch (Exception e) {
            log.error("[{}] CRITICAL: Failed to save audit log to SQL Server. Reason: {}", correlationId, e.getMessage(), e);
        }
    }
}