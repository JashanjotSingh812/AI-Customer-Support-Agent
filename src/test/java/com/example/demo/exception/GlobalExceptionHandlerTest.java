package com.example.demo.exception;

import io.github.resilience4j.ratelimiter.RateLimiter;
import io.github.resilience4j.ratelimiter.RateLimiterConfig;
import io.github.resilience4j.ratelimiter.RequestNotPermitted;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler exceptionHandler;

    @BeforeEach
    void setUp() {
        exceptionHandler = new GlobalExceptionHandler();
    }

    @Test
    void shouldHandleBusinessRuleException() {

        BusinessRuleException exception =
                new BusinessRuleException(
                        "Order cannot be cancelled"
                );

        Map<String, String> response =
                exceptionHandler.handleBusinessRule(exception);

        assertNotNull(response);

        assertEquals(
                "Order cannot be cancelled",
                response.get("error")
        );
    }

    @Test
    void shouldReturnCorrectBusinessRuleErrorKey() {

        BusinessRuleException exception =
                new BusinessRuleException(
                        "Order not found"
                );

        Map<String, String> response =
                exceptionHandler.handleBusinessRule(exception);

        assertEquals(1, response.size());

        assertEquals(
                "Order not found",
                response.get("error")
        );
    }

    @Test
    void shouldHandleRateLimitException() {

        RateLimiter rateLimiter =
                mock(RateLimiter.class);

        RateLimiterConfig config =
                RateLimiterConfig.custom()
                        .limitForPeriod(5)
                        .limitRefreshPeriod(
                                java.time.Duration.ofSeconds(10)
                        )
                        .timeoutDuration(
                                java.time.Duration.ZERO
                        )
                        .build();

        when(rateLimiter.getRateLimiterConfig())
                .thenReturn(config);

        RequestNotPermitted exception =
                RequestNotPermitted.createRequestNotPermitted(
                        rateLimiter
                );

        Map<String, String> response =
                exceptionHandler.handleRateLimit(exception);

        assertNotNull(response);

        assertEquals(
                "Too many requests. Please try again later.",
                response.get("error")
        );
    }

    @Test
    void shouldReturnCorrectRateLimitErrorKey() {

        RateLimiter rateLimiter =
                mock(RateLimiter.class);

        RateLimiterConfig config =
                RateLimiterConfig.custom()
                        .limitForPeriod(5)
                        .limitRefreshPeriod(
                                java.time.Duration.ofSeconds(10)
                        )
                        .timeoutDuration(
                                java.time.Duration.ZERO
                        )
                        .build();

        when(rateLimiter.getRateLimiterConfig())
                .thenReturn(config);

        RequestNotPermitted exception =
                RequestNotPermitted.createRequestNotPermitted(
                        rateLimiter
                );

        Map<String, String> response =
                exceptionHandler.handleRateLimit(exception);

        assertEquals(1, response.size());

        assertEquals(
                "Too many requests. Please try again later.",
                response.get("error")
        );
    }
}
