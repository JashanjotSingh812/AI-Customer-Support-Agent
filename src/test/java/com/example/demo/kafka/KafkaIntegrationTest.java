package com.example.demo.kafka;

import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.junit.jupiter.api.Test;
import org.testcontainers.kafka.KafkaContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

import static org.assertj.core.api.Assertions.assertThat;

@Testcontainers
class KafkaIntegrationTest {

    @Container
    static KafkaContainer kafka =
            new KafkaContainer("apache/kafka:4.0.0");

    @Test
    void shouldPublishAndConsumeKafkaMessage() {

        String topic = "test-topic-" + UUID.randomUUID();

        try (
                var producer =
                        new org.apache.kafka.clients.producer.KafkaProducer<>(
                                Map.of(
                                        ProducerConfig.BOOTSTRAP_SERVERS_CONFIG,
                                        kafka.getBootstrapServers(),
                                        ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG,
                                        StringSerializer.class,
                                        ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG,
                                        StringSerializer.class
                                )
                        );

                Consumer<String, String> consumer =
                        new KafkaConsumer<>(
                                Map.of(
                                        ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG,
                                        kafka.getBootstrapServers(),
                                        ConsumerConfig.GROUP_ID_CONFIG,
                                        "test-group-" + UUID.randomUUID(),
                                        ConsumerConfig.AUTO_OFFSET_RESET_CONFIG,
                                        "earliest",
                                        ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG,
                                        StringDeserializer.class,
                                        ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG,
                                        StringDeserializer.class
                                )
                        )
        ) {

            consumer.subscribe(List.of(topic));

            producer.send(
                    new org.apache.kafka.clients.producer.ProducerRecord<>(
                            topic,
                            "ticket-1",
                            "Ticket created"
                    )
            ).get();

            producer.flush();

            ConsumerRecords<String, String> records =
                    ConsumerRecords.empty();

            long timeout = System.currentTimeMillis() + 10_000;

            while (
                    records.isEmpty()
                            && System.currentTimeMillis() < timeout
            ) {
                records = consumer.poll(Duration.ofMillis(500));
            }

            assertThat(records)
                    .isNotEmpty();

            assertThat(records.iterator().next().key())
                    .isEqualTo("ticket-1");

            assertThat(records.iterator().next().value())
                    .isEqualTo("Ticket created");
        } catch (ExecutionException e) {
            throw new RuntimeException(e);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}