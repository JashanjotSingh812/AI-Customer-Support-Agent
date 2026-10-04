package com.example.demo.controller;

import com.example.demo.service.ExternalService;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.CompletionStage;

@RestController
public class ExternalServiceController {

    private final ExternalService externalService;

    public ExternalServiceController(ExternalService externalService) {
        this.externalService = externalService;
    }

    @GetMapping("/api/test/external")
    @RateLimiter(name = "chatApi")
    public CompletionStage<String> callExternalService() {
        return externalService.callExternalService();
    }
}