package ca.zhoozhoo.loaddev.rifles.config;

import static java.util.Collections.emptyList;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import org.jspecify.annotations.NonNull;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.convert.converter.Converter;
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
 * Security configuration for the rifles service.
 * <p>
 * Configures OAuth2 resource server with JWT validation and Keycloak permission
 * extraction. Converts Keycloak permissions to Spring Security authorities.
 * Active in all profiles except 'test'.
 *
 * @author Zhubin Salehi
 */
@Configuration
@EnableWebFluxSecurity
@EnableReactiveMethodSecurity
@Profile("!test")
public class SecurityConfiguration {

    /**
     * Configures security filter chain with public actuator/docs access
     * and authenticated access for all other endpoints.
     *
     * @param http the ServerHttpSecurity to configure
     * @return the configured SecurityWebFilterChain
     */
    @Bean
    public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {
        return http
                .authorizeExchange(authorize -> authorize
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
     * JWT authentication converter that extracts authorities from Keycloak permissions.
     *
     * @return converter that transforms JWT tokens into authentication objects
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
     * Extracts granted authorities from Keycloak JWT 'authorization' claim.
     * Authority format: "resourceName:scope".
     */
    static class KeycloakPermissionsConverter implements Converter<Jwt, Collection<GrantedAuthority>> {

        @Override
        public Collection<GrantedAuthority> convert(@NonNull Jwt jwt) {
            var authorization = jwt.getClaimAsMap("authorization");
            if (authorization == null) {
                return emptyList();
            }

            if (!(authorization.get("permissions") instanceof List<?> rawPermissions)) {
                return emptyList();
            }

            // Convert Keycloak permissions to Spring Security GrantedAuthorities
            return rawPermissions.stream()
                    .filter(permission -> permission instanceof Map<?, ?>)
                    .map(permission -> (Map<?, ?>) permission)
                    .filter(permission -> permission.get("rsname") != null)
                    .flatMap(permission -> {                        
                        return switch (permission.get("scopes")) {
                            case Collection<?> rawScopes -> rawScopes.stream()
                                    .filter(scope -> scope instanceof String)
                                    .map(scope -> "%s:%s".formatted(permission.get("rsname").toString(), scope))
                                    .map(SimpleGrantedAuthority::new);
                            case null, default -> Stream.empty();
                        };
                    })
                    .map(authority -> (GrantedAuthority) authority)
                    .toList();
        }
    }
}