package com.medgo.loaservice.domain.dto.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class LoaDownloadResponseDTO {
    private Data data;
    private String statusCode;
    private String statusName;

    @lombok.Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Data {
        private String createdOn;
        private String createdBy;
        private String fileName;

        @JsonProperty("fileBase64")
        private String fileBase64;
    }
}
