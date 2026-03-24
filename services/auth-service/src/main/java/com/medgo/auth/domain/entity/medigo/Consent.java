package com.medgo.auth.domain.entity.medigo;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.hibernate.annotations.ColumnDefault;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "CONSENT", uniqueConstraints = {
    @UniqueConstraint(name = "CONSENT_pk", columnNames = {"VERSION_NO", "TYPE"})
})
@EqualsAndHashCode(callSuper = true)
public class Consent extends BaseEntity {
    
    @Column(name = "TYPE", nullable = false, length = 100)
    private String type;
    
    @Column(name = "VERSION_NO", nullable = false, length = 10)
    private String versionNo;
    
    @Column(name = "HEADER", nullable = false, length = 250)
    private String header;
    
    @Column(name = "CONTENT", columnDefinition = "VARCHAR(MAX)")
    private String content;
    
    @Column(name = "ENABLED")
    @ColumnDefault("1")
    private Integer enabled;
    
    @Column(name = "REQUIRED")
    @ColumnDefault("0")
    private Integer required;
    
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
}

