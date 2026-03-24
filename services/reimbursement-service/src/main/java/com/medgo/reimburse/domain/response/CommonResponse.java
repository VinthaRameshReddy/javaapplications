package com.medgo.reimburse.domain.response;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Collections;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
@NoArgsConstructor
@AllArgsConstructor
public class CommonResponse {

    @JsonProperty("statusCode")
    private String statusCode;   // <-- changed from int to String

    @JsonProperty("response")
    private String response;

    @JsonProperty("data")
    private Object data;

    @JsonProperty("traceId")
//    private String traceId = RequestContext.getTraceId();
    private String traceId = "Doesn't matter";

    // keep error in data, not as separate field
    public static CommonResponse success(Object data) {
        CommonResponse response = new CommonResponse();
        response.statusCode = "000";
        response.response = "SUCCESS";
        if (data instanceof String) {
            response.data = Collections.singletonMap("message", data);
        } else {
            response.data = data;
        }
        return response;
    }

    public static CommonResponse error(ErrorResponse error, int statusCode) {
        CommonResponse response = new CommonResponse();
        response.statusCode = "001";
        response.response = "ERROR";
        response.data = error;
        return response;
    }

    // Add this method to the CommonResponse class
    @JsonIgnore
    public boolean isSuccess() {
        return "000".equals(this.statusCode); // Assuming "000" indicates success
    }
}
