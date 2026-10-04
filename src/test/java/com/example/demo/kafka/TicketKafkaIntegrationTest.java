package com.example.demo.kafka;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Import;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.context.TestPropertySource;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Import(TicketKafkaIntegrationTest.TestKafkaConsumer.class)
@TestPropertySource(properties = {
        "spring.kafka.bootstrap-servers=localhost:9092",
        "spring.kafka.consumer.group-id=ticket-kafka-integration-test",
        "JWT_SECRET=integration-test-secret-key-must-be-long-enough-123456789",
        "knowledge-base.initializer.enabled=false"
})
class TicketKafkaIntegrationTest {

    @Autowired
    private KafkaTemplate<String, Object> kafkaTemplate;

    @Autowired
    private TestKafkaConsumer testKafkaConsumer;

    @BeforeEach
    void setUp() {
        testKafkaConsumer.reset();
    }

    @Test
    void shouldPublishAndConsumeTicketCreatedEvent()
            throws Exception {

        TicketCreatedEvent event =
                new TicketCreatedEvent(
                        999L,
                        501L,
                        1001L,
                        "Product arrived damaged",
                        "OPEN"
                );

        kafkaTemplate
                .send(
                        "ticket-created",
                        "999",
                        event
                )
                .get(10, TimeUnit.SECONDS);

        boolean received =
                testKafkaConsumer
                        .getLatch()
                        .await(10, TimeUnit.SECONDS);

        assertTrue(
                received,
                "Kafka event was not received"
        );

        TicketCreatedEvent receivedEvent =
                testKafkaConsumer.getReceivedEvent();

        assertNotNull(receivedEvent);

        assertEquals(
                999L,
                receivedEvent.ticketId()
        );

        assertEquals(
                501L,
                receivedEvent.customerId()
        );

        assertEquals(
                1001L,
                receivedEvent.orderId()
        );

        assertEquals(
                "Product arrived damaged",
                receivedEvent.issue()
        );

        assertEquals(
                "OPEN",
                receivedEvent.status()
        );
    }

    @TestConfiguration
    static class TestKafkaConsumer {

        private CountDownLatch latch =
                new CountDownLatch(1);

        private volatile TicketCreatedEvent receivedEvent;

        @KafkaListener(
                topics = "ticket-created",
                groupId = "ticket-kafka-integration-test"
        )
        public void consume(
                ConsumerRecord<String, TicketCreatedEvent> record
        ) {
            receivedEvent = record.value();
            latch.countDown();
        }

        void reset() {
            latch = new CountDownLatch(1);
            receivedEvent = null;
        }

        CountDownLatch getLatch() {
            return latch;
        }

        TicketCreatedEvent getReceivedEvent() {
            return receivedEvent;
        }
    }
}