package com.medgo.notification.feign;

import com.medgo.notification.config.FeignClientConfig;
import com.medgo.notification.model.EmailRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.Map;

@FeignClient(
        name = "shared-notification-service",
        url = "${shared.notification.service.url}",
        configuration = FeignClientConfig.class)
public interface SharedNotificationServiceClient {

    @PostMapping("/notification/v1/send")
    ResponseEntity<Map<String, String>> sendEmail(@RequestBody EmailRequest emailRequest);
}


