// src/main/java/com/medicard/integration/domain/audit/EmailAuditLog.java
package com.medicard.integration.domain.entity;

import com.medicard.integration.domain.request.EmailStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "EMAIL_AUDIT_LOG")
@Getter
@Setter
public class EmailAuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Maps to UNIQUEIDENTIFIER in SQL Server
    @Column(name = "CORRELATION_ID", nullable = false, updatable = false)
    private UUID correlationId;

    @Column(name = "ATTEMPT_NUMBER", nullable = false)
    private int attemptNumber;

    @Enumerated(EnumType.STRING)
    @Column(name = "STATUS", nullable = false)
    private EmailStatus status;

    @Column(name = "TIMESTAMP", nullable = false, updatable = false)
    private Instant timestamp = Instant.now();

    @Column(name = "DETAILS", columnDefinition = "NVARCHAR(MAX)")
    private String details;

    @Column(name = "SUBJECT", length = 512)
    private String subject;

    @Column(name = "RECIPIENTS", columnDefinition = "NVARCHAR(MAX)")
    private String recipients;

    @Column(name = "emailtype")
    private String Type;
}