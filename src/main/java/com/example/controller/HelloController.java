package com.example.controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HelloController {
    @GetMapping("/hello")
    public String hello() {
        return "Hello from Spring Boot Application!";
    }

    @GetMapping("/api/status")
    public String status() {
        return "Application is running successfully";
    }

    @PostMapping("/api/echo")
    public String echo(@RequestBody String message) {
        return "Echo: " + message;
    }
}