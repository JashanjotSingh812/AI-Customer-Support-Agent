package com.example.demo.observability;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.springframework.stereotype.Service;

@Service
public class MetricsService {

    private final Counter ticketsCreated;
    private final Counter ordersCancelled;
    private final Counter aiRequests;
    private final Timer aiResponseTime;

    public MetricsService(MeterRegistry meterRegistry) {

        ticketsCreated = Counter.builder("support.tickets.created")
                .description("Number of support tickets created")
                .register(meterRegistry);

        ordersCancelled = Counter.builder("support.orders.cancelled")
                .description("Number of orders cancelled")
                .register(meterRegistry);

        aiRequests = Counter.builder("support.ai.requests")
                .description("Number of AI support requests")
                .register(meterRegistry);

        aiResponseTime = Timer.builder("support.ai.response.time")
                .description("AI response latency")
                .register(meterRegistry);
    }

    public void ticketCreated() {
        ticketsCreated.increment();
    }

    public void orderCancelled() {
        ordersCancelled.increment();
    }

    public void aiRequest() {
        aiRequests.increment();
    }

    public Timer.Sample startAiTimer() {
        return Timer.start();
    }

    public void recordAiResponse(Timer.Sample sample) {
        sample.stop(aiResponseTime);
    }
}