package com.medgo.commons;

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
    private String traceId = RequestContext.getTraceId();

    // keep error in data, not as separate field
    public static CommonResponse success(Object data) {
        CommonResponse response = new CommonResponse();
        response.statusCode = String.valueOf(200); // HTTP 200 OK
        response.response = "SUCCESS";
        if (data instanceof String) {
            response.data = Collections.singletonMap("message", data);
        } else {
            response.data = data;
        }
        return response;
    }

    // Overloaded method to allow custom HTTP status code for success
    public static CommonResponse success(Object data, int httpStatusCode) {
        CommonResponse response = new CommonResponse();
        response.statusCode = String.valueOf(httpStatusCode);
        response.response = "SUCCESS";
        if (data instanceof String) {
            response.data = Collections.singletonMap("message", data);
        } else {
            response.data = data;
        }
        return response;
    }

    public static CommonResponse successWithMemberCode(String memberCode, String message) {
        CommonResponse response = new CommonResponse();
        response.statusCode = String.valueOf(200); // HTTP 200 OK
        response.response = "SUCCESS";
        response.data = new MemberData(memberCode, message); // wrap member code inside object
        return response;
    }

    public static CommonResponse error(ErrorResponse error, int statusCode) {
        CommonResponse response = new CommonResponse();
        response.statusCode = String.valueOf(statusCode); // Use the HTTP status code passed as parameter
        response.response = "ERROR";
        response.data = error;
        return response;
    }

    // Add this method to the CommonResponse class
    @JsonIgnore
    public boolean isSuccess() {
        return "200".equals(this.statusCode); // HTTP 200 indicates success
    }
}
