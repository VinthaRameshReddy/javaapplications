package com.medgo.bean;


import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AppInfo {
    @JsonProperty("appid")
    private String appId;
    @JsonProperty("appver")
    private String appVer;
    @JsonProperty("channel")
    private String channel;
}