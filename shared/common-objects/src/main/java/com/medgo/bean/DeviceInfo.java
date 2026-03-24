package com.medgo.bean;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DeviceInfo {
    @JsonProperty("deviceip")
    private String deviceIp;
    @JsonProperty("devicelat")
    private String deviceLat;
    @JsonProperty("devicelong")
    private String deviceLong;
    @JsonProperty("devicemac")
    private String deviceMac;
    @JsonProperty("devicemodel")
    private String deviceModel;
    @JsonProperty("deviceos")
    private String deviceOS;
    @JsonProperty("deviceos_version")
    private String deviceosVersion;
}
