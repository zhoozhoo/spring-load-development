package ca.zhoozhoo.loaddev.common.opentelemetry;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.http.HttpHeaders;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;

import reactor.core.publisher.Mono;

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