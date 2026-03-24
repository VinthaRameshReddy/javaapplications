package com.medgo.reimburse.feign;

import com.medgo.reimburse.config.NotificationFeignConfig;
import com.medgo.reimburse.domain.dto.EmailRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.Map;

@FeignClient(
        name = "notification-service",
        url = "${notification.service.url}",
        configuration = NotificationFeignConfig.class)
public interface NotificationServiceClient {

    @PostMapping("/notification/send")
    ResponseEntity<Map<String, Object>> sendEmail(@RequestBody EmailRequest emailRequest);
}

