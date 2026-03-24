package com.medgo.auth.domain.entity.medigo;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.hibernate.annotations.ColumnDefault;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "USER_CONSENT")
@EqualsAndHashCode(callSuper = true)
public class UserConsent extends BaseEntity {
    
    @Column(name = "USER_ID", nullable = false)
    private String userId;
    
    @Column(name = "CONSENT_ID", nullable = false)
    private Integer consentId;
    
    @Column(name = "AGREED")
    private Integer agreed;
    
    @Column(name = "MEMBER_CODE", length = 50)
    private String memberCode;
    
    @Column(name = "MOBILE", length = 11)
    private String mobile;
    
    @Column(name = "CREATED_ON", updatable = false)
    @ColumnDefault("getdate()")
    private LocalDateTime createdOn;
    
    @Column(name = "CREATED_BY", updatable = false, length = 50)
    @ColumnDefault("'System'")
    private String createdBy;
    
    @Column(name = "UPDATED_ON")
    private LocalDateTime updatedOn;
    
    @Column(name = "UPDATED_BY", length = 50)
    private String updatedBy;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "CONSENT_ID", insertable = false, updatable = false)
    private Consent consent;
}

