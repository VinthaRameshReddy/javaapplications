package com.medgo.notification.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmailRequest {
    private String subject;
    private String body;
    private List<String> toEmails;
    private String type;
    private List<String> ccEmails;
    private List<String> bccEmails;
    private String contentType;
    private List<AttachmentRequest> attachments;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AttachmentRequest {
        private String filename;
        private String content; // base64
    }
}


