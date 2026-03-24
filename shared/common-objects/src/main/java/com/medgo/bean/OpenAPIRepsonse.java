package com.medgo.bean;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.util.MultiValueMap;

import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class OpenAPIRepsonse {
    @JsonProperty("statuscode")
    private String statusCode;
    @JsonProperty("response")
    private String response;
    @JsonProperty("resdata")
    private Map<String, Object> responseData;
    @JsonIgnore
    private MultiValueMap<String, String> responseHeader;

}
