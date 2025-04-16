package ca.zhoozhoo.loaddev.api.config;

import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;

import lombok.extern.log4j.Log4j2;

/**
 * Gateway filter factory that forwards the permission token to downstream services.
 * This filter retrieves the permission token from the exchange attributes and adds it
 * to the Authorization header of the request.
 */
@Component
@Log4j2
public class TokenForwardingGatewayFilterFactory
        extends AbstractGatewayFilterFactory<TokenForwardingGatewayFilterFactory.Config> {

    public TokenForwardingGatewayFilterFactory() {
        super(Config.class);
    }

    /**
     * Applies the filter logic to the exchange.
     * Retrieves the permission token from the exchange attributes,
     * adds it to the Authorization header, and forwards the request.
     *
     * @param config the configuration for this filter
     * @return a GatewayFilter that modifies the request header
     */
    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            String token = exchange.getAttribute("permission_token");
            if (token != null) {
                ServerHttpRequest request = exchange.getRequest().mutate()
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                        .build();
                exchange = exchange.mutate().request(request).build();
                log.debug("Forwarding request with permission token to: {}", request.getPath());
            }
            return chain.filter(exchange);
        };
    }

    public static class Config {
        // Empty config class as we don't need any configuration
    }
}