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
     */
    static class KeycloakPermissionsConverter implements Converter<Jwt, Collection<GrantedAuthority>> {

        @Override
        public Collection<GrantedAuthority> convert(@NonNull Jwt jwt) {
            var authorization = jwt.getClaimAsMap("authorization");
            if (authorization == null) {
                return emptyList();
            }

            @SuppressWarnings("unchecked")
            var permissions = (List<Map<String, Object>>) authorization.get("permissions");
            if (permissions == null) {
                return emptyList();
            }

            // Convert Keycloak permissions to Spring Security GrantedAuthorities
            return permissions.stream()
                    .filter(permission -> permission.get("rsname") != null)
                    .flatMap(permission -> {
                        var resourceName = permission.get("rsname").toString();
                        @SuppressWarnings("unchecked")
                        var scopes = (Collection<String>) permission.get("scopes");

                        if (scopes == null) {
                            return Stream.empty();
                        }

                        return scopes.stream()
                                .map(scope -> String.format("%s:%s", resourceName, scope))
                                .map(SimpleGrantedAuthority::new);
                    })
                    .collect(toList());
        }
    }
}