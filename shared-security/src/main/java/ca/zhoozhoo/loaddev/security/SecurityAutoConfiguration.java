package ca.zhoozhoo.loaddev.security;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.security.autoconfigure.SecurityProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.config.annotation.method.configuration.EnableReactiveMethodSecurity;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.ReactiveJwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.ReactiveJwtGrantedAuthoritiesConverterAdapter;
import org.springframework.security.web.server.SecurityWebFilterChain;

import reactor.core.publisher.Mono;

/**
 * Auto-configuration for Spring Security OAuth2 Resource Server with reactive WebFlux.
 * <p>
 * Provides default security configuration with:
 * <ul>
 *   <li>JWT authentication with Keycloak permission extraction</li>
 *   <li>Configurable public paths (actuator, swagger, etc.)</li>
 *   <li>Custom JWT principal claim support</li>
 *   <li>Method-level security enabled</li>
 * </ul>
 * <p>
 * Configure via {@code security.*} properties in application configuration.
 *
 * @author Zhubin Salehi
 * @see SecurityProperties
 * @see CurrentUserMethodArgumentResolver
 */
@AutoConfiguration
@ConditionalOnClass(ServerHttpSecurity.class)
@EnableWebFluxSecurity
@EnableReactiveMethodSecurity
public class SecurityAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public Converter<Jwt, Mono<AbstractAuthenticationToken>> jwtAuthenticationConverter() {
        var jwtAuthConverter = new ReactiveJwtAuthenticationConverter();
        jwtAuthConverter.setJwtGrantedAuthoritiesConverter(
                new ReactiveJwtGrantedAuthoritiesConverterAdapter(new KeycloakPermissionsConverter()));
        jwtAuthConverter.setPrincipalClaimName( "sub");
        return jwtAuthConverter;
    }

    @Bean
    @ConditionalOnMissingBean
    public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http,
            Converter<Jwt, Mono<AbstractAuthenticationToken>> jwtConverter) {
        return http
                .authorizeExchange(exchanges -> exchanges
                        .pathMatchers(
                                "/actuator/**",
                                "/swagger-ui.html",
                                "/swagger-ui/**",
                                "/v3/api-docs",
                                "/v3/api-docs/**")
                        .permitAll()
                        .anyExchange().authenticated())
                .oauth2ResourceServer(oauth2 -> oauth2.jwt(jwt -> jwt.jwtAuthenticationConverter(jwtConverter)))
                .build();
    }
}
