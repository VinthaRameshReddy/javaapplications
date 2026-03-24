package com.medgo.bean;


import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Request {
    @JsonProperty("appinfo")
    private AppInfo appInfo;
    @JsonProperty("deviceinfo")
    private DeviceInfo deviceInfo;
    @JsonProperty("reqdata")
    private Map<String, Object> reqData; //object //encrypation
}
