package com.medgo.loaservice.domain.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AttachmentDTO {

    @NotBlank(message = "Filename is required.")
    @JsonProperty("filename")
    private String filename;

    @NotBlank(message = "File Base64 is required.")
    @JsonProperty("fileBase64")
    private String fileBase64;

    // Optional fields for internal use
    private byte[] bytes;
    private String contentType;
    private Long fileSizeBytes;
    private String fileSize;

    private Long claimNatureId;
    private Long claimTypeId;
    private Long claimDocumentId;
}
