package com.medgo.filemanagement.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/test")
public class TestController {

    @GetMapping("/ping")
    public String ping() {
        return " File Management Service is up and running!";
    }

    @GetMapping("/hello")
    public String hello() {
        return "Hello from TestController 👋";
    }
}
