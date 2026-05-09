package com.example.security.monitoring;

import io.micrometer.core.instrument.Timer;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
@RequiredArgsConstructor
public class MetricsInterceptor implements HandlerInterceptor {

    private final MetricsService metricsService;
    private final ThreadLocal<Timer.Sample> timerSample = new ThreadLocal<>();

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        timerSample.set(metricsService.startTimer());
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        String endpoint = request.getRequestURI();
        String method = request.getMethod();
        String status = String.valueOf(response.getStatus());

        metricsService.incrementRequestCount(endpoint, method, status);

        if (timerSample.get() != null) {
            metricsService.stopTimer(timerSample.get(), endpoint, method);
            timerSample.remove();
        }

        if (ex != null) {
            metricsService.incrementErrorCount(endpoint, ex.getClass().getSimpleName());
        }
    }
}