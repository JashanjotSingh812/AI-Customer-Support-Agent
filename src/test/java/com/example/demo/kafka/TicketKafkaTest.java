package com.example.demo.kafka;

import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.kafka.support.serializer.JsonSerializer;
import org.testcontainers.kafka.KafkaContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.Duration;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

import static org.assertj.core.api.Assertions.assertThat;

@Testcontainers
class TicketKafkaTest {

    @Container
    static KafkaContainer kafka =
            new KafkaContainer("apache/kafka:4.0.0");

    private static Consumer<String, TicketCreatedEvent> consumer;

    @BeforeAll
    static void setUpConsumer() {

        JsonDeserializer<TicketCreatedEvent> valueDeserializer =
                new JsonDeserializer<>(TicketCreatedEvent.class);

        valueDeserializer.addTrustedPackages(
                "com.example.demo.kafka"
        );

        Map<String, Object> properties = Map.of(
                ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG,
                kafka.getBootstrapServers(),

                ConsumerConfig.GROUP_ID_CONFIG,
                "ticket-kafka-test-" + UUID.randomUUID(),

                ConsumerConfig.AUTO_OFFSET_RESET_CONFIG,
                "earliest"
        );

        DefaultKafkaConsumerFactory<String, TicketCreatedEvent>
                consumerFactory =
                new DefaultKafkaConsumerFactory<>(
                        properties,
                        new StringDeserializer(),
                        valueDeserializer
                );

        consumer = consumerFactory.createConsumer();

        consumer.subscribe(
                java.util.List.of("ticket-created")
        );
    }

    @AfterAll
    static void tearDownConsumer() {

        if (consumer != null) {
            consumer.close();
        }
    }

    @Test
    void shouldPublishAndConsumeTicketCreatedEvent() throws ExecutionException, InterruptedException {

        JsonSerializer<TicketCreatedEvent> serializer =
                new JsonSerializer<>();

        org.apache.kafka.clients.producer.KafkaProducer<
                String,
                TicketCreatedEvent
                > producer =
                new org.apache.kafka.clients.producer.KafkaProducer<>(
                        Map.of(
                                org.apache.kafka.clients.producer.ProducerConfig
                                        .BOOTSTRAP_SERVERS_CONFIG,
                                kafka.getBootstrapServers(),

                                org.apache.kafka.clients.producer.ProducerConfig
                                        .KEY_SERIALIZER_CLASS_CONFIG,
                                org.apache.kafka.common.serialization
                                        .StringSerializer.class,

                                org.apache.kafka.clients.producer.ProducerConfig
                                        .VALUE_SERIALIZER_CLASS_CONFIG,
                                JsonSerializer.class
                        )
                );

        TicketCreatedEvent event =
                new TicketCreatedEvent(
                        1L,
                        501L,
                        1001L,
                        "Product is damaged",
                        "OPEN"
                );

        try {

            producer.send(
                    new org.apache.kafka.clients.producer.ProducerRecord<>(
                            "ticket-created",
                            "1",
                            event
                    )
            ).get();

            producer.flush();

            ConsumerRecords<String, TicketCreatedEvent> records =
                    ConsumerRecords.empty();

            long timeout =
                    System.currentTimeMillis() + 10_000;

            while (
                    records.isEmpty()
                            && System.currentTimeMillis() < timeout
            ) {

                records =
                        consumer.poll(
                                Duration.ofMillis(500)
                        );
            }

            assertThat(records)
                    .isNotEmpty();

            TicketCreatedEvent received =
                    records.iterator().next().value();

            assertThat(received.ticketId())
                    .isEqualTo(1L);

            assertThat(received.customerId())
                    .isEqualTo(501L);

            assertThat(received.orderId())
                    .isEqualTo(1001L);

            assertThat(received.issue())
                    .isEqualTo("Product is damaged");

            assertThat(received.status())
                    .isEqualTo("OPEN");

        } finally {

            producer.close();
        }
    }
}