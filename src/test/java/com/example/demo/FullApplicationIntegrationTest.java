package com.example.demo;

import com.example.demo.entity.Order;
import com.example.demo.repository.OrderRepository;
import com.example.demo.security.JwtService;
import com.example.demo.service.KnowledgeBaseService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.web.client.RestClient;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.io.IOException;
import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@Testcontainers
@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = {
                "JWT_SECRET=test-secret-key-that-is-at-least-32-characters-long"
        }
)
class FullApplicationIntegrationTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres =
            new PostgreSQLContainer<>("pgvector/pgvector:pg17")
                    .withDatabaseName("test_db")
                    .withUsername("test")
                    .withPassword("test");

    @LocalServerPort
    private int port;

    @MockitoBean
    private VectorStore vectorStore;

    @MockitoBean
    private KnowledgeBaseService knowledgeBaseService;

    @MockitoBean
    private JwtService jwtService;

    @Autowired
    private OrderRepository orderRepository;

    private RestClient restClient;

    @BeforeEach
    void setUp() {

        restClient = RestClient.builder()
                .baseUrl("http://localhost:" + port)
                .build();

        Claims claims = Jwts.claims()
                .subject("customer501")
                .add("customerId", 501L)
                .build();

        when(jwtService.extractClaims("test-token"))
                .thenReturn(claims);
    }

    @Test
    void shouldGetOrderForAuthenticatedCustomer() {

        Order order = new Order(
                1001L,
                501L,
                "iPhone 17",
                "SHIPPED",
                BigDecimal.valueOf(79999)
        );

        // Save test data through the real repository.
        // If your Order entity generates the ID automatically,
        // remove the first constructor argument.
        Order savedOrder = orderRepository.save(order);

        var response = restClient.get()
                .uri("/api/chat/orders/{orderId}", savedOrder.getId())
                .header(
                        HttpHeaders.AUTHORIZATION,
                        "Bearer test-token"
                )
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .toEntity(Order.class);

        assertThat(response.getStatusCode().is2xxSuccessful())
                .isTrue();

        assertThat(response.getBody())
                .isNotNull();

        assertThat(response.getBody().getId())
                .isEqualTo(1001L);

        assertThat(response.getBody().getCustomerId())
                .isEqualTo(501L);

        assertThat(response.getBody().getProductName())
                .isEqualTo("iPhone 17");

        assertThat(response.getBody().getStatus())
                .isEqualTo("SHIPPED");

        assertThat(response.getBody().getAmount())
                .isEqualByComparingTo(
                        BigDecimal.valueOf(79999)
                );
    }

    @Test
    void shouldRejectRequestWithoutAuthentication() throws IOException {

        var response = restClient.get()
                .uri("/api/chat/orders/1001")
                .exchange(
                        (request, clientResponse) -> clientResponse
                );

        assertThat(response.getStatusCode().value())
                .isEqualTo(401);
    }

    @Test
    void shouldPreventCustomerFromAccessingAnotherCustomersOrder() throws IOException {

        Order order = new Order(
                2001L,
                502L,
                "Dell Laptop",
                "DELIVERED",
                BigDecimal.valueOf(65000)
        );

        orderRepository.save(order);

        var response = restClient.get()
                .uri("/api/chat/orders/2001")
                .header(
                        HttpHeaders.AUTHORIZATION,
                        "Bearer test-token"
                )
                .exchange(
                        (request, clientResponse) -> clientResponse
                );

        assertThat(response.getStatusCode().value())
                .isEqualTo(400);
    }
}