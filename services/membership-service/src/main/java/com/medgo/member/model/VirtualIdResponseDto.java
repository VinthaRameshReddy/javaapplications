package com.medgo.member.model;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.List;

@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class VirtualIdResponseDto implements Serializable {

    private String status;
    private String message;
    private List<Data> data;

    @Getter
    @Setter
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class Data {
        //response when on queue
        private String jobId;
        private List<Queue> data;

        //response when virtualId is generated
        private String url;
        private String expiresOn;
        private Meta meta;
        private String type;
    }

    @Getter
    @Setter
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class Queue {

        private String batchCode;
        private String accountCode;
        private String accountName;
        private String memberCode;
        private String fullName;
//        private String gender;
//        private String age;
//        private String birthDate;
//        private String effectivityDate;
//        private String validityDate;
        private List<String> types;
        private String template;
    }

    @Getter
    @Setter
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class Meta {

        @JsonProperty("jobid")
        private String jobid;
        @JsonProperty("reviewdate")
        private String reviewdate;
        private String reviewer;
        private String template;
        @JsonProperty("uploaddate")
        private String uploaddate;
    }
}
