package ca.zhoozhoo.loaddev.loads.config;

import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.toList;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.convert.converter.Converter;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.config.annotation.method.configuration.EnableReactiveMethodSecurity;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.ReactiveJwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.ReactiveJwtGrantedAuthoritiesConverterAdapter;
import org.springframework.security.web.server.SecurityWebFilterChain;

import reactor.core.publisher.Mono;

/**
 * Security configuration for the loads service.
 * Configures WebFlux security with OAuth2 resource server support.
 * This configuration is active in all profiles except 'test'.
 * 
 * @author Zhubin Salehi
 */
@Configuration
@EnableWebFluxSecurity
@EnableReactiveMethodSecurity
@Profile("!test")
public class SecurityConfiguration {

    /**
     * Configures the security filter chain for the application.
     * Permits access to actuator endpoints and requires authentication for all
     * other requests.
     *
     * @param http the ServerHttpSecurity to configure
     * @return the configured SecurityWebFilterChain
     */
    @Bean
    public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {
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
                .oauth2ResourceServer(
                        oauth2 -> oauth2.jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthenticationConverter())))
                .build();
    }

    /**
     * Creates a JWT authentication converter that extracts authorities from
     * Keycloak permissions.
     *
     * @return a converter that transforms JWT tokens into authentication objects
     */
    @Bean
    Converter<Jwt, Mono<AbstractAuthenticationToken>> jwtAuthenticationConverter() {
        var jwtAuthenticationConverter = new ReactiveJwtAuthenticationConverter();
        jwtAuthenticationConverter.setJwtGrantedAuthoritiesConverter(
                new ReactiveJwtGrantedAuthoritiesConverterAdapter(new KeycloakPermissionsConverter()));
        jwtAuthenticationConverter.setPrincipalClaimName("sub"); // Ensure subject claim is used for principal

        return jwtAuthenticationConverter;
    }

    /**
     * Custom converter to extract granted authorities from Keycloak JWT tokens.
     * Processes the 'authorization' claim and converts permissions into
     * GrantedAuthority objects.
     * The format of the granted authority is "resourceName:scope".
     * <p>
     * Uses Java 25 enhanced pattern matching for cleaner type checks and casts.
     * </p>
     */
    static class KeycloakPermissionsConverter implements Converter<Jwt, Collection<GrantedAuthority>> {

        @Override
        public Collection<GrantedAuthority> convert(@NonNull Jwt jwt) {
            // Use pattern matching to safely extract and validate authorization claim
            if (!(jwt.getClaims().get("authorization") instanceof Map<?, ?> authorization)) {
                return emptyList();
            }

            // Use pattern matching for permissions extraction
            if (!(authorization.get("permissions") instanceof List<?> permissions)) {
                return emptyList();
            }

            // Convert Keycloak permissions to Spring Security GrantedAuthorities with enhanced pattern matching
            return permissions.stream()
                    .filter(permission -> permission instanceof Map<?, ?>)
                    .map(permission -> (Map<?, ?>) permission)
                    .filter(permission -> permission.get("rsname") instanceof String)
                    .flatMap(permission -> {
                        // Pattern match for scopes collection
                        if (!(permission.get("scopes") instanceof Collection<?> scopes)) {
                            return Stream.empty();
                        }

                        return scopes.stream()
                                .filter(scope -> scope instanceof String)
                                .map(scope -> "%s:%s".formatted(permission.get("rsname"), scope))
                                .map(SimpleGrantedAuthority::new);
                    })
                    .collect(toList());
        }
    }
}