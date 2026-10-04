package com.example.demo.controller;

import io.micrometer.tracing.Span;
import io.micrometer.tracing.Tracer;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TracingTestController {

    private final Tracer tracer;

    public TracingTestController(Tracer tracer) {
        this.tracer = tracer;
    }

    @GetMapping("/api/test/tracing")
    public String testTracing() {

        Span currentSpan = tracer.currentSpan();

        if (currentSpan == null) {
            return "NO ACTIVE SPAN";
        }

        return "traceId=" + currentSpan.context().traceId()
                + ", spanId=" + currentSpan.context().spanId();
    }
}