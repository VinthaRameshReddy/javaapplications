package com.medgo.bean;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Timestamp;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class APIAuditLogger {
    private String entityId;
    private String tradingAccountId;
    private String clientIP;
    private String userName;
    private String userId;
    private String sourceID;
    private String appVersion;
    private String deviceOS;
    private String deviceID;
    private String ctToken;
    private String geoLocation;
    private String apiURI;
    private String subType;
    private String methodType;
    private String traceId;
    private String serviceName;
    private String apiName;
    private String responseCode;
    private String apiRequest;
    private String apiResponse;
    private long totalResponseTime;
    private Timestamp startTime;
    private Timestamp endTime;
}
