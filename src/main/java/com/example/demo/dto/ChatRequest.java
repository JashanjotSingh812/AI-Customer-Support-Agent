package com.example.demo.dto;

public record ChatRequest(
        String conversationId,
        String message
) {
}