package com.medicard.integration.controller;

import com.medicard.integration.domain.request.EmailRequest;
import com.medicard.integration.service.GraphEmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/v1")

public class NotificationServiceController {

    private final GraphEmailService emailService;


    @PostMapping("/send")
    public ResponseEntity<Map<String, String>> sendEmail(@RequestBody EmailRequest emailRequest) {
        try {
            emailService.sendEmail(emailRequest);
            Map<String, String> response = new HashMap<>();
            response.put("message", "Email sent successfully");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error sending email: {}", e.getMessage());
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "Failed to send email: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
}