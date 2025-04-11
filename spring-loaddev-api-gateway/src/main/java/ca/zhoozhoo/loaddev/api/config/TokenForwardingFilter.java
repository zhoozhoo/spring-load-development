package ca.zhoozhoo.loaddev.api.config;

import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpHeaders;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;

import lombok.extern.log4j.Log4j2;
import reactor.core.publisher.Mono;

/**
 * A global filter that forwards permission tokens to downstream services.
 * This filter runs after the PermissionTokenExchangeFilter and forwards the 
 * exchanged permission token by adding it to the Authorization header of the request.
 */
@Component
@Order(1)  // Explicit order after PermissionTokenExchangeFilter
@Log4j2
public class TokenForwardingFilter implements GlobalFilter, Ordered {

    /**
     * Filters incoming requests by forwarding the permission token.
     * This method retrieves the permission token from exchange attributes
     * and adds it to the Authorization header of the forwarded request.
     *
     * @param exchange the current server exchange
     * @param chain the filter chain
     * @return Mono<Void> representing the completion of the filter operation
     */
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        return Mono.just(exchange)
                .map(this::updateRequestWithPermissionToken)
                .flatMap(chain::filter)
                .onErrorResume(e -> {
                    log.error("Error in token forwarding", e);
                    return chain.filter(exchange);
                });
    }

    /**
     * Updates the request with the permission token if available.
     * Creates a new request with the permission token in the Authorization header.
     *
     * @param exchange the current server exchange containing the permission token
     * @return updated ServerWebExchange with the new Authorization header
     */
    private ServerWebExchange updateRequestWithPermissionToken(ServerWebExchange exchange) {
        String token = exchange.getAttribute("permission_token");
        if (token != null) {
            ServerHttpRequest request = exchange.getRequest().mutate()
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                    .build();
            exchange = exchange.mutate().request(request).build();
            log.debug("Forwarding request with permission token to: {}", request.getPath());
        }
        return exchange;
    }

    /**
     * Defines the order of this filter in the filter chain.
     * Order is set to 1 to ensure it runs after PermissionTokenExchangeFilter (order 0).
     */
    @Override
    public int getOrder() {
        return 1;
    }
}