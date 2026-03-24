package com.medgo.reimburse.domain.entity.medigo;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "reimbursement_documents")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReimbursementDocumentEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reimbursement_request_id", nullable = false)
    private ReimbursementRequestEntity reimbursementRequest;

    @Column(name = "document_type", nullable = false, length = 100)
    private String documentType;

    @Column(name = "file_name", nullable = false, length = 255)
    private String fileName;

    @Column(name = "file_size")
    private Long fileSize;

    @Column(name = "blob_url", nullable = false, length = 1000)
    private String blobUrl;

    @Column(name = "blob_name", nullable = false, length = 500)
    private String blobName;

    @Column(name = "content_type", length = 100)
    private String contentType;

    @Column(name = "created_at", nullable = false)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
