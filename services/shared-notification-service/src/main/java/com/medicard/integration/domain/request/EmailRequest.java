package com.medicard.integration.domain.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.medicard.integration.validation.ValidEmailList;
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

    @JsonProperty("subject")
    private String subject;

    @JsonProperty("body")
    private String body;

    @JsonProperty("toEmails")
    @ValidEmailList
    private List<String> toEmails;

    @JsonProperty("type")
    private String type;

    @JsonProperty("ccEmails")
    private List<String> ccEmails;

    @JsonProperty("bccEmails")
    private List<String> bccEmails;

    @JsonProperty("contentType")
    private String contentType;

    // --- NEW FIELD FOR ATTACHMENTS ---
    @JsonProperty("attachments")
    private List<AttachmentRequest> attachments;

    /**
     * A nested DTO to represent a single file attachment.
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AttachmentRequest {
        @JsonProperty("filename")
        private String filename;

        // Content will be the Base64 encoded string of the file
        @JsonProperty("content")
        private String content;
    }
}