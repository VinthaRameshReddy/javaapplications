package com.medgo.member.config;

import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Configuration;



@Configuration
@EnableFeignClients(basePackages = "com.medgo.member.feign") // ONLY your feign interfaces
public class FeignConfig {}
