package com.example.demo.service;

import com.example.demo.exception.BusinessRuleException;
import com.example.demo.kafka.TicketEventProducer;
import com.example.demo.entity.Order;
import com.example.demo.entity.Ticket;
import com.example.demo.observability.MetricsService;
import com.example.demo.repository.OrderRepository;
import com.example.demo.repository.TicketRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TicketServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private TicketRepository ticketRepository;

    @Mock
    private TicketEventProducer ticketEventProducer;

    @Mock
    private MetricsService metricsService;

    @InjectMocks
    private TicketService ticketService;


    // ============================================================
    // TEST 1
    // Customer should not create a ticket for another customer's order
    // ============================================================

    @Test
    void shouldRejectTicketCreationWhenCustomerDoesNotOwnOrder() {

        // Arrange
        Long orderId = 1002L;
        Long authenticatedCustomerId = 501L;

        Order order = new Order(
                1002L,
                502L,
                "Dell Laptop",
                "DELIVERED",
                BigDecimal.valueOf(65000.0)
        );

        when(orderRepository.findById(orderId))
                .thenReturn(Optional.of(order));

        // Act + Assert
        assertThrows(
                BusinessRuleException.class,
                () -> ticketService.createTicket(
                        authenticatedCustomerId,
                        orderId,
                        "Laptop is damaged"
                )
        );

        // Ticket should never be saved
        verify(ticketRepository, never())
                .save(any());

        // Kafka event should never be published
        verify(ticketEventProducer, never())
                .publishTicketCreated(any());
    }


    // ============================================================
    // TEST 2
    // Duplicate request should return existing ticket
    // ============================================================

    @Test
    void shouldReturnExistingTicketWhenDuplicateRequestIsReceived() {

        // Arrange
        Long customerId = 501L;
        Long orderId = 1001L;
        String issue = "Missing cable";

        Order order = new Order(
                1001L,
                501L,
                "iPhone 17",
                "SHIPPED",
                BigDecimal.valueOf(79999.0)
        );

        String idempotencyKey =
                customerId + ":" + orderId + ":" + issue;

        Ticket existingTicket = new Ticket(
                customerId,
                orderId,
                issue,
                "OPEN",
                LocalDateTime.now(),
                idempotencyKey
        );

        when(orderRepository.findById(orderId))
                .thenReturn(Optional.of(order));

        when(ticketRepository.findByIdempotencyKey(idempotencyKey))
                .thenReturn(Optional.of(existingTicket));

        // Act
        Ticket result = ticketService.createTicket(
                customerId,
                orderId,
                issue
        );

        // Assert
        assertSame(existingTicket, result);

        // No new ticket should be created
        verify(ticketRepository, never())
                .save(any());

        // No duplicate Kafka event
        verify(ticketEventProducer, never())
                .publishTicketCreated(any());

        // No new metric
        verify(metricsService, never())
                .ticketCreated();
    }


    // ============================================================
    // TEST 3
    // Successfully create a new ticket
    // ============================================================

    @Test
    void shouldCreateTicketSuccessfully() {

        // Arrange
        Long customerId = 501L;
        Long orderId = 1001L;
        String issue = "Missing cable";

        Order order = new Order(
                1001L,
                501L,
                "iPhone 17",
                "SHIPPED",
                BigDecimal.valueOf(79999.0)
        );

        String idempotencyKey =
                customerId + ":" + orderId + ":" + issue;

        Ticket savedTicket = new Ticket(
                customerId,
                orderId,
                issue,
                "OPEN",
                LocalDateTime.now(),
                idempotencyKey
        );

        when(orderRepository.findById(orderId))
                .thenReturn(Optional.of(order));

        when(ticketRepository.findByIdempotencyKey(idempotencyKey))
                .thenReturn(Optional.empty());

        when(ticketRepository.save(any(Ticket.class)))
                .thenReturn(savedTicket);

        // Act
        Ticket result = ticketService.createTicket(
                customerId,
                orderId,
                issue
        );

        // Assert
        assertSame(savedTicket, result);

        // Ticket must be saved
        verify(ticketRepository)
                .save(any(Ticket.class));

        // Metric should be incremented
        verify(metricsService)
                .ticketCreated();

        // Kafka event should be published
        verify(ticketEventProducer)
                .publishTicketCreated(savedTicket);
    }


    // ============================================================
    // TEST 4
    // Order does not exist
    // ============================================================

    @Test
    void shouldRejectTicketCreationWhenOrderDoesNotExist() {

        // Arrange
        Long customerId = 501L;
        Long orderId = 9999L;

        when(orderRepository.findById(orderId))
                .thenReturn(Optional.empty());

        // Act + Assert
        assertThrows(
                BusinessRuleException.class,
                () -> ticketService.createTicket(
                        customerId,
                        orderId,
                        "Order issue"
                )
        );

        // Nothing should be saved
        verify(ticketRepository, never())
                .save(any());

        // No Kafka event
        verify(ticketEventProducer, never())
                .publishTicketCreated(any());
    }
}
