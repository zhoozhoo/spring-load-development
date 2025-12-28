package ca.zhoozhoo.loaddev.common.opentelemetry;

import org.jspecify.annotations.Nullable;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;

import io.micrometer.tracing.TraceContext;
import io.micrometer.tracing.Tracer;
import reactor.core.publisher.Mono;

/**
 * WebFlux filter that adds the current trace ID to outgoing HTTP response headers.
 * 
 * <p>This filter intercepts every HTTP response and injects the OpenTelemetry trace ID
 * as an {@code X-Trace-Id} header. This enables clients and API consumers to correlate
 * client-side events with server-side traces for end-to-end debugging and monitoring.</p>
 * 
 * <p><b>Use Cases:</b></p>
 * <ul>
 *   <li><b>Client-Side Error Reporting:</b> When a client encounters an error, it can include
 *       the X-Trace-Id in error reports, allowing support teams to locate the exact server
 *       trace and associated logs.</li>
 *   <li><b>Performance Analysis:</b> Clients can measure end-to-end latency and correlate it
 *       with server-side trace data to identify whether delays occur in the network, client,
 *       or server.</li>
 *   <li><b>API Documentation:</b> API consumers can use the trace ID to reference specific
 *       requests when reporting issues or asking for support.</li>
 *   <li><b>Load Testing:</b> Performance testing tools can collect trace IDs to analyze
 *       individual request performance in distributed tracing systems.</li>
 * </ul>
 * 
 * <p><b>Example Response Header:</b></p>
 * <pre>{@code
 * HTTP/1.1 200 OK
 * Content-Type: application/json
 * X-Trace-Id: a1b2c3d4e5f6g7h8i9j0k1l2m3n4o5p6
 * ...
 * }</pre>
 * 
 * <p><b>Integration with Observability Stack:</b></p>
 * <p>The trace ID in the response header matches the {@code trace_id} in:</p>
 * <ul>
 *   <li>Server logs (via Log4j2 MDC)</li>
 *   <li>Distributed traces in Tempo</li>
 *   <li>Metrics in Prometheus (as trace exemplars)</li>
 *   <li>Grafana dashboards for unified observability</li>
 * </ul>
 * 
 * @author Zhubin Salehi
 * @see Tracer
 * @see TraceContext
 * @see FilterConfiguration
 */
public class AddTraceIdFilter implements WebFilter {
    private final Tracer tracer;

    /**
     * Constructs a filter with the provided Micrometer tracer.
     * 
     * @param tracer the tracer for accessing current trace context
     */
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