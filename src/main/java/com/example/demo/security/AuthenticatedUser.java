package com.example.demo.security;

public record AuthenticatedUser(
        Long customerId,
        String username
) {
}