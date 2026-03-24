package com.medgo.claims.config;

import feign.Logger;
import feign.RequestInterceptor;
import feign.Retryer;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.multipart.MultipartFile;

import java.util.concurrent.TimeUnit;


@Configuration
public class FileManagementFeignConfig {

    @Bean
    public Retryer feignRetryer() {
        return new Retryer.Default(1000, TimeUnit.SECONDS.toMillis(5), 6);
    }


    @Bean
    public Logger.Level feignLoggerLevel() {
        return Logger.Level.FULL;
    }

    @Bean
    public RequestInterceptor headerPropagationInterceptor() {
        return requestTemplate -> requestTemplate.header("Postman-Token", "bypass-encryption");
    }
    @FeignClient(name = "file-management-service", url = "filemanagement.service.url", configuration = FileManagementFeignConfig.class)
    public interface FileManagementServiceClient {


    }
}

