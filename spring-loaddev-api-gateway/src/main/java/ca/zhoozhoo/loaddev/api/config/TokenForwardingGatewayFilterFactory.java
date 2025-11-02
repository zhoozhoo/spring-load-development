package ca.zhoozhoo.loaddev.api.config;

import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;

import lombok.extern.log4j.Log4j2;

/**
 * Spring Cloud Gateway filter factory that forwards permission tokens to downstream services.
 * 
 * <p>This filter retrieves the permission token that was stored in the exchange attributes by
 * {@link PermissionTokenExchangeFilter} and adds it to the Authorization header of outgoing
 * requests to backend microservices. This ensures that downstream services receive the
 * enhanced permission token rather than the original user token.</p>
 * 
 * <p><b>Usage in Gateway Routes:</b></p>
 * <pre>{@code
 * spring:
 *   cloud:
 *     gateway:
 *       routes:
 *         - id: my-service
 *           uri: lb://my-service
 *           filters:
 *             - TokenForwarding
 * }</pre>
 * 
 * <p>The filter works in conjunction with {@link PermissionTokenExchangeFilter}:
 * <ol>
 *   <li>PermissionTokenExchangeFilter exchanges user token for permission token</li>
 *   <li>Permission token is stored in exchange attributes with key "permission_token"</li>
 *   <li>This filter retrieves the token and adds it to the Authorization header</li>
 *   <li>Request is forwarded to downstream service with permission token</li>
 * </ol>
 * 
 * <p>If no permission token is found in the exchange attributes, the request proceeds
 * without modification, preserving any existing Authorization header.</p>
 * 
 * @author Zhubin Salehi
 * @see PermissionTokenExchangeFilter
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
     * @param config the configuration for this filter (unused, empty record)
     * @return a GatewayFilter that modifies the request header
     */
    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            String token = exchange.getAttribute("permission_token");
            if (token != null) {
                ServerHttpRequest request = exchange.getRequest().mutate()
                        .header(HttpHeaders.AUTHORIZATION, "Bearer %s".formatted(token))
                        .build();
                exchange = exchange.mutate().request(request).build();
                log.debug("Forwarding request with permission token to: {}", request.getPath());
            }
            return chain.filter(exchange);
        };
    }

    /**
     * Configuration record for the TokenForwarding filter.
     * Currently empty as no configuration parameters are required.
     * Using a record (JDK 16+) instead of a class for immutability and conciseness.
     */
    public record Config() {
        // Empty record - no configuration needed
    }
}