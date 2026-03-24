package com.medgo.auth.commonutilitys;

import com.medgo.commons.CommonResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;

import java.util.HashMap;
import java.util.Map;

/**
 * Utility class for handling response headers, especially JWT tokens
 */
public class ResponseHeaderUtil {
    private static final Logger logger = LoggerFactory.getLogger(ResponseHeaderUtil.class);
    private static final String ACCESS_TOKEN_HEADER = "X-Access-Token";
    private static final String ACCESS_TOKEN_KEY = "accessToken";
    private static final String MESSAGE_KEY = "message";

    /**
     * Processes response to move accessToken to header and optionally return only message in body
     * 
     * @param response The CommonResponse from service
     * @param returnOnlyMessage If true, returns only message in body; if false, returns all data except token
     * @return ResponseEntity with token in header and modified body
     */
    public static ResponseEntity<CommonResponse> processTokenResponse(CommonResponse response, boolean returnOnlyMessage) {
        HttpHeaders headers = new HttpHeaders();
        CommonResponse modifiedResponse = response;

        if (response.getData() instanceof Map<?, ?> dataMap) {
            @SuppressWarnings("unchecked")
            Map<String, Object> responseData = (Map<String, Object>) dataMap;
            Object accessToken = responseData.get(ACCESS_TOKEN_KEY);

            if (accessToken != null) {
                // Add token to header
                headers.set(ACCESS_TOKEN_HEADER, accessToken.toString());
                logger.info("Added {} to response header", ACCESS_TOKEN_HEADER);

                if (returnOnlyMessage) {
                    // Only return message
                    Map<String, Object> modifiedData = new HashMap<>();
                    Object message = responseData.get(MESSAGE_KEY);
                    if (message != null) {
                        modifiedData.put(MESSAGE_KEY, message);
                    }
                    modifiedResponse = CommonResponse.success(modifiedData);
                } else {
                    // Return all data except token
                    Map<String, Object> modifiedData = new HashMap<>(responseData);
                    modifiedData.remove(ACCESS_TOKEN_KEY);
                    modifiedResponse = CommonResponse.success(modifiedData);
                }
                modifiedResponse.setTraceId(response.getTraceId());
            }
        }

        return ResponseEntity.ok().headers(headers).body(modifiedResponse);
    }

    /**
     * Processes response to move accessToken to header and return all data except token in body
     * 
     * @param response The CommonResponse from service
     * @return ResponseEntity with token in header and all data except token in body
     */
    public static ResponseEntity<CommonResponse> processTokenResponse(CommonResponse response) {
        return processTokenResponse(response, false);
    }
}

