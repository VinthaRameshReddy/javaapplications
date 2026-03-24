package com.medgo.auth.domain.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.medgo.auth.validation.ValidEmailList;

import java.util.List;

public record EmailRequest(

        @JsonProperty("subject")
        String subject,

        @JsonProperty("body")
        String body,

        @JsonProperty("toEmails")
        @ValidEmailList
        List<String> toEmails,

        @JsonProperty("type")
        String type,

        @JsonProperty("ccEmails")
        List<String> ccEmails,

        @JsonProperty("bccEmails")
        List<String> bccEmails,

        @JsonProperty("contentType")
        String contentType,

        @JsonProperty("attachments")
        List<AttachmentRequest> attachments
) {
    public record AttachmentRequest(
            @JsonProperty("filename")
            String filename,

            @JsonProperty("content")
            String content
    ) {
    }
}
