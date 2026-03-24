package com.medgo.reimburse.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FileUploadResponse {
    private String blobUrl;
    private String blobName;
    private String fileName;
    private Long fileSize;
    private String contentType;
}
