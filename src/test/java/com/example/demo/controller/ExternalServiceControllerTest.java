package com.example.demo.controller;

import org.junit.jupiter.api.Test;
import org.springframework.boot.resttestclient.TestRestTemplate;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.ResponseEntity;
import org.springframework.test.annotation.DirtiesContext;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = {
                "JWT_SECRET=ThisIsAValidTestSecretKeyForJwt123456789",
                "knowledge-base.initializer.enabled=false",
                "resilience4j.ratelimiter.instances.chatApi.limit-for-period=5",
                "resilience4j.ratelimiter.instances.chatApi.limit-refresh-period=10s",
                "resilience4j.ratelimiter.instances.chatApi.timeout-duration=0"
        }
)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class ExternalServiceControllerTest {

    @LocalServerPort
    private int port;

    private final TestRestTemplate restTemplate =
            new TestRestTemplate();

    private String url() {
        return "http://localhost:" + port + "/api/test/external";
    }

    @Test
    void shouldAllowFirstFiveRequests() {

        for (int i = 1; i <= 5; i++) {

            ResponseEntity<String> response =
                    restTemplate.getForEntity(
                            url(),
                            String.class
                    );

            System.out.println(
                    "Request " + i +
                            " -> " + response.getStatusCode()
            );

            assertEquals(
                    200,
                    response.getStatusCode().value(),
                    "Request " + i + " should be allowed"
            );
        }
    }

    @Test
    void shouldRejectRequestAfterRateLimitExceeded() {

        // Consume the 5 available permits
        for (int i = 1; i <= 5; i++) {

            ResponseEntity<String> response =
                    restTemplate.getForEntity(
                            url(),
                            String.class
                    );

            System.out.println(
                    "Request " + i +
                            " -> " + response.getStatusCode()
            );

            assertEquals(
                    200,
                    response.getStatusCode().value(),
                    "Request " + i + " should be allowed"
            );
        }

        // 6th request must be rejected
        ResponseEntity<String> response =
                restTemplate.getForEntity(
                        url(),
                        String.class
                );

        System.out.println(
                "Request 6 -> " + response.getStatusCode()
        );

        assertEquals(
                429,
                response.getStatusCode().value(),
                "Request 6 should be rate limited"
        );
    }
}