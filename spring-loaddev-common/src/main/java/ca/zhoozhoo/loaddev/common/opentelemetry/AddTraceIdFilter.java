package ca.zhoozhoo.loaddev.common.opentelemetry;

import org.jspecify.annotations.Nullable;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;

import io.micrometer.tracing.TraceContext;
import io.micrometer.tracing.Tracer;
import reactor.core.publisher.Mono;

public class AddTraceIdFilter implements WebFilter {
    private final Tracer tracer;

    AddTraceIdFilter(Tracer tracer) {
        this.tracer = tracer;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        String traceId = getTraceId();
        if (traceId != null) {
            exchange.getResponse().getHeaders().add("X-Trace-Id", traceId);
        }
        return chain.filter(exchange);
    }

    private @Nullable String getTraceId() {
        TraceContext context = this.tracer.currentTraceContext().context();
        return context != null ? context.traceId() : null;
    }
}