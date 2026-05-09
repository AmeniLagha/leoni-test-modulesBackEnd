package com.example.security.monitoring;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MetricsService {

    private final MeterRegistry meterRegistry;

    public void incrementRequestCount(String endpoint, String method, String status) {
        meterRegistry.counter("http.requests",
                "endpoint", endpoint,
                "method", method,
                "status", status
        ).increment();
    }

    public Timer.Sample startTimer() {
        return Timer.start(meterRegistry);
    }

    public void stopTimer(Timer.Sample sample, String endpoint, String method) {
        sample.stop(Timer.builder("http.request.duration")
                .tag("endpoint", endpoint)
                .tag("method", method)
                .register(meterRegistry));
    }

    public void incrementErrorCount(String endpoint, String errorType) {
        meterRegistry.counter("application.errors",
                "endpoint", endpoint,
                "error_type", errorType
        ).increment();
    }
}