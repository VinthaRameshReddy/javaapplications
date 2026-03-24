package com.medgo.claims.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class
DocumentResponse {
    private Long id; // Document ID from database (used for removal)
    private String documentType;
    private String fileName;
    private String blobUrl;
    private Long fileSize;
}












