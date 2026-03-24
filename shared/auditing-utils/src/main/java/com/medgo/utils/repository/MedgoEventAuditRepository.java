package com.medgo.utils.repository;

import com.medgo.utils.domain.MedgoEventAudit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface MedgoEventAuditRepository extends JpaRepository<MedgoEventAudit, Long> {
    
    /**
     * Find all audit logs by username
     */
    List<MedgoEventAudit> findByUsernameOrderByEventTimeDesc(String username);
    
    /**
     * Find all audit logs by event type
     */
    List<MedgoEventAudit> findByEventTypeOrderByEventTimeDesc(String eventType);
    
    /**
     * Find all audit logs by username and event type
     */
    List<MedgoEventAudit> findByUsernameAndEventTypeOrderByEventTimeDesc(String username, String eventType);
    
    /**
     * Find all audit logs within a date range
     */
    @Query("SELECT a FROM MedgoEventAudit a WHERE a.eventTime BETWEEN :startTime AND :endTime ORDER BY a.eventTime DESC")
    List<MedgoEventAudit> findByEventTimeBetween(@Param("startTime") LocalDateTime startTime, 
                                                   @Param("endTime") LocalDateTime endTime);
    
    /**
     * Find all audit logs by username within a date range
     */
    @Query("SELECT a FROM MedgoEventAudit a WHERE a.username = :username AND a.eventTime BETWEEN :startTime AND :endTime ORDER BY a.eventTime DESC")
    List<MedgoEventAudit> findByUsernameAndEventTimeBetween(@Param("username") String username,
                                                              @Param("startTime") LocalDateTime startTime,
                                                              @Param("endTime") LocalDateTime endTime);
    
    /**
     * Find all audit logs by device ID
     */
    List<MedgoEventAudit> findByDeviceIdOrderByEventTimeDesc(String deviceId);
    
    /**
     * Count audit logs by event type
     */
    long countByEventType(String eventType);
    
    /**
     * Count audit logs by username
     */
    long countByUsername(String username);
}
