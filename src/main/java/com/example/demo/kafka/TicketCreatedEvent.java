package com.example.demo.kafka;

public record TicketCreatedEvent(
        Long ticketId,
        Long customerId,
        Long orderId,
        String issue,
        String status
) {
}