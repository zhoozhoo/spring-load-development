package ca.zhoozhoo.loaddev.common.opentelemetry;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.micrometer.tracing.Tracer;

/**
 * Configuration for registering WebFlux filters that enhance observability and debugging.
 * 
 * <p>This configuration registers two key filters in the reactive filter chain:</p>
 * <ul>
 *   <li>{@link HeaderLoggerFilter} - Logs all incoming HTTP request headers for debugging</li>
 *   <li>{@link AddTraceIdFilter} - Adds trace ID to outgoing response headers for correlation</li>
 * </ul>
 * 
 * <p>These filters run early in the WebFlux filter chain, before Spring Security filters
 * and before the request reaches controller handlers. This ensures that:</p>
 * <ol>
 *   <li>Request headers are logged immediately when the request arrives</li>
 *   <li>Trace context is available and captured before any async operations</li>
 *   <li>Response headers include trace ID even if the request fails early in processing</li>
 * </ol>
 * 
 * <p><b>Development vs Production:</b></p>
 * <p>In development environments, header logging helps debug authentication issues, content
 * negotiation, and trace propagation. In production, consider setting the header logger
 * to DEBUG level to reduce log volume while maintaining the trace ID response header
 * for client-side correlation.</p>
 * 
 * @author Zhubin Salehi
 * @see HeaderLoggerFilter
 * @see AddTraceIdFilter
 */
@Configuration(proxyBeanMethods = false)
public class FilterConfiguration {

    /**
     * Creates a filter that logs all incoming HTTP request headers.
     * 
     * <p>Useful for debugging authentication, content negotiation, and trace propagation issues.
     * Set log level to DEBUG in production to reduce verbosity.</p>
     * 
     * @return filter for logging request headers
     */
    @Bean
    HeaderLoggerFilter headerLoggerFilter() {
        return new HeaderLoggerFilter();
    }

    /**
     * Creates a filter that adds the current trace ID to response headers.
     * 
     * <p>Enables clients to correlate their requests with server-side traces and logs
     * for comprehensive debugging and performance analysis.</p>
     * 
     * @param tracer the Micrometer tracer for accessing current trace context
     * @return filter for adding X-Trace-Id response header
     */
    @Bean
    AddTraceIdFilter addTraceIdFilter(Tracer tracer) {
        return new AddTraceIdFilter(tracer);
    }

}