package com.example.demo.integration;

import com.example.demo.entity.Customer;
import com.example.demo.entity.Order;
import com.example.demo.entity.Ticket;
import com.example.demo.repository.CustomerRepository;
import com.example.demo.repository.OrderRepository;
import com.example.demo.repository.TicketRepository;
import org.junit.jupiter.api.Test;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Testcontainers
@SpringBootTest(properties = {
        "JWT_SECRET=test-secret-key-that-is-at-least-32-characters-long",
})
class PostgresIntegrationTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres =
            new PostgreSQLContainer<>("pgvector/pgvector:pg17")
                    .withDatabaseName("test_db")
                    .withUsername("test")
                    .withPassword("test");

    @MockitoBean
    private VectorStore vectorStore;

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private TicketRepository ticketRepository;

    @Test
    void shouldSaveAndReadCustomer() {

        Customer customer = new Customer(
                9001L,
                "Test Customer",
                "test@example.com",
                "9999999999"
        );

        customerRepository.save(customer);

        Optional<Customer> result =
                customerRepository.findById(9001L);

        assertTrue(result.isPresent());

        Customer savedCustomer = result.get();

        assertEquals(
                9001L,
                savedCustomer.getId()
        );

        assertEquals(
                "Test Customer",
                savedCustomer.getName()
        );

        assertEquals(
                "test@example.com",
                savedCustomer.getEmail()
        );
    }

    @Test
    void shouldSaveAndReadOrder() {

        Order order = new Order(
                9001L,
                501L,
                "Test iPhone",
                "PLACED",
                BigDecimal.valueOf(79999)
        );

        orderRepository.save(order);

        Optional<Order> result =
                orderRepository.findById(9001L);

        assertTrue(result.isPresent());

        Order savedOrder = result.get();

        assertEquals(
                9001L,
                savedOrder.getId()
        );

        assertEquals(
                501L,
                savedOrder.getCustomerId()
        );

        assertEquals(
                "Test iPhone",
                savedOrder.getProductName()
        );

        assertEquals(
                "PLACED",
                savedOrder.getStatus()
        );

        assertEquals(
                0,
                BigDecimal.valueOf(79999)
                        .compareTo(savedOrder.getAmount())
        );
    }

    @Test
    void shouldSaveAndReadTicket() {

        Ticket ticket = new Ticket(
                501L,
                9001L,
                "Product arrived damaged",
                "OPEN",
                LocalDateTime.now(),
                "integration-test-key"
        );

        ticketRepository.save(ticket);

        Optional<Ticket> result =
                ticketRepository.findByIdempotencyKey(
                        "integration-test-key"
                );

        assertTrue(result.isPresent());

        Ticket savedTicket = result.get();

        assertEquals(501L, savedTicket.getCustomerId());
        assertEquals(9001L, savedTicket.getOrderId());
        assertEquals(
                "Product arrived damaged",
                savedTicket.getIssue()
        );
        assertEquals("OPEN", savedTicket.getStatus());
        assertEquals(
                "integration-test-key",
                savedTicket.getIdempotencyKey()
        );
    }
}