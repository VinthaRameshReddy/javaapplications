package com.medgo.notification.controller;

import com.medgo.notification.feign.SharedNotificationServiceClient;
import com.medgo.notification.model.EmailRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequiredArgsConstructor
//@RequestMapping("/notification")
public class NotificationProxyController {

    private final SharedNotificationServiceClient client;

    @PostMapping("/send")
    public ResponseEntity<Map<String, String>> send(@RequestBody EmailRequest request) {
        return client.sendEmail(request);
    }
}


