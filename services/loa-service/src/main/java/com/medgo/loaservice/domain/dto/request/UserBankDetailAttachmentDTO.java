package com.medgo.loaservice.domain.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserBankDetailAttachmentDTO {

    @NotBlank(message = "Filename is required.")
    private String filename;

    @NotBlank(message = "File Base64 is required.")
    private String fileBase64;

    private Boolean enabled;

    //for internal use
    private Long id;
    private byte[] bytes;
    private String contentType;
    private Long fileSizeBytes;
    private String fileSize;
}
