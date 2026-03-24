// src/main/java/com/medicard/integration/repository/EmailAuditRepository.java
package com.medicard.integration.repository;

import com.medicard.integration.domain.entity.EmailAuditLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EmailAuditRepository extends JpaRepository<EmailAuditLog, Long> {
}