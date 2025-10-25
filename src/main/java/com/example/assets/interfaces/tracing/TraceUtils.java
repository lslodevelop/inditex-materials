package com.example.assets.interfaces.tracing;

import io.micrometer.tracing.Tracer;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Component
@RequiredArgsConstructor
public class TraceUtils {

    private final Tracer tracer;

    public String getCurrentTraceId() {
        return tracer.currentSpan() != null ? Objects.requireNonNull(tracer.currentSpan()).context().traceId() : "N/A";
    }
}
