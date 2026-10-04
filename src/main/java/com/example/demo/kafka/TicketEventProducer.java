package com.example.demo.kafka;

import com.example.demo.entity.Ticket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class TicketEventProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    private static final Logger log =
            LoggerFactory.getLogger(TicketEventProducer.class);

    public TicketEventProducer(
            KafkaTemplate<String, Object> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void publishTicketCreated(Ticket ticket) {

        TicketCreatedEvent event = new TicketCreatedEvent(
                ticket.getId(),
                ticket.getCustomerId(),
                ticket.getOrderId(),
                ticket.getIssue(),
                ticket.getStatus()
        );

        log.info(
                "Publishing ticket-created event | ticketId={} | orderId={}",
                ticket.getId(),
                ticket.getOrderId()
        );

        kafkaTemplate
                .send(
                        "ticket-created",
                        ticket.getId().toString(),
                        event
                )
                .whenComplete((result, exception) -> {

                    if (exception != null) {

                        log.error(
                                "Kafka publish failed | ticketId={} | error={}",
                                ticket.getId(),
                                exception.getMessage(),
                                exception
                        );

                    } else {

                        log.info(
                                "Kafka event published | ticketId={} | topic={} | partition={} | offset={}",
                                ticket.getId(),
                                result.getRecordMetadata().topic(),
                                result.getRecordMetadata().partition(),
                                result.getRecordMetadata().offset()
                        );
                    }
                });
    }
}