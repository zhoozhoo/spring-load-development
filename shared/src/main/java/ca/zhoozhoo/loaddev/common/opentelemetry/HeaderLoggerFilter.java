package ca.zhoozhoo.loaddev.common.opentelemetry;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.http.HttpHeaders;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;

import reactor.core.publisher.Mono;

/**
 * WebFlux filter that logs all incoming HTTP request headers for debugging and monitoring.
 * 
 * <p>This filter intercepts every HTTP request and logs all headers at DEBUG level.
 * It's particularly useful for:</p>
 * <ul>
 *   <li><b>Authentication Debugging:</b> Verify that Authorization headers, API keys,
 *       or custom authentication tokens are correctly sent by clients.</li>
 *   <li><b>Content Negotiation:</b> Inspect Accept, Accept-Language, and Content-Type
 *       headers to debug API response format issues.</li>
 *   <li><b>Trace Propagation:</b> Verify that distributed tracing headers (traceparent,
 *       tracestate) are correctly propagated from upstream services.</li>
 *   <li><b>CORS Debugging:</b> Check Origin and custom CORS headers when troubleshooting
 *       cross-origin requests.</li>
 *   <li><b>Cache Control:</b> Verify cache-related headers for API caching strategies.</li>
 * </ul>
 * 
 * <p><b>Logging Configuration:</b></p>
 * <p>Headers are logged using Log4j2 at DEBUG level. In production environments,
 * set the logger level to DEBUG only when investigating specific issues:</p>
 * <pre>{@code
 * <Logger name="ca.zhoozhoo.loaddev.common.opentelemetry.HeaderLoggerFilter" level="debug"/>
 * }</pre>
 * 
 * <p><b>Security Considerations:</b></p>
 * <p>Be cautious with sensitive headers in logs. Consider filtering or masking headers like:
 * Authorization, Cookie, X-API-Key, and custom authentication headers when logging to
 * centralized log aggregation systems.</p>
 * 
 * @author Zhubin Salehi
 * @see FilterConfiguration
 */
class HeaderLoggerFilter implements WebFilter {
    private static final Logger LOGGER = LogManager.getLogger(HeaderLoggerFilter.class);

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        logHeaders(exchange.getRequest().getHeaders());
        return chain.filter(exchange);
    }

    private void logHeaders(HttpHeaders headers) {
        headers.forEach((header, values) -> {
            LOGGER.debug("{}: {}", header, values.size() == 1 ? values.getFirst() : values);
        });
    }
}