package com.example.demo.kafka;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class TicketEventConsumer {

    private static final Logger log =
            LoggerFactory.getLogger(TicketEventConsumer.class);

    @KafkaListener(
            topics = "ticket-created",
            groupId = "support-agent-test"
    )
    public void consume(TicketCreatedEvent event) {

        log.info(
                "Ticket created event received | ticketId={} | customerId={} | orderId={} | status={}",
                event.ticketId(),
                event.customerId(),
                event.orderId(),
                event.status()
        );

        // Business processing goes here
    }
}