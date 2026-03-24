package com.medgo.auth.clients;

import com.medgo.auth.config.FeignClientConfig;
import com.medgo.auth.domain.request.EmailRequest;
import jakarta.validation.Valid;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.Map;

@FeignClient(name = "notification-service",
        url = "${url:https://medgo2o-stg.medicardphils.com}",
        configuration = FeignClientConfig.class)
public interface NotificationServiceClient {
    @PostMapping("/notification/send")
    ResponseEntity<Map<String, String>> sendEmail(@Valid @RequestBody EmailRequest emailRequest);
}