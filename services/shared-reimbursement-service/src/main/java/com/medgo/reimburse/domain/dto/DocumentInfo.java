package com.medgo.reimburse.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DocumentInfo {
    private Long id;
    private String documentType;
    private String fileName;
    private String blobUrl;
    private String blobName;
    private Long fileSize;
    private String contentType;
}
