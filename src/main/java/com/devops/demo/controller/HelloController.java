package com.devops.demo.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HelloController {

    @GetMapping("/")
    public String home() {
        return "Java CI/CD Pipeline Working Successfully!";
    }

    @GetMapping("/health")
    public String health() {
        return "Application Healthy";
    }
}