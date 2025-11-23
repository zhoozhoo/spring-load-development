package ca.zhoozhoo.loaddev.security;

import java.util.List;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
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
@EnableConfigurationProperties(SecurityProperties.class)
public class SecurityAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public Converter<Jwt, Mono<AbstractAuthenticationToken>> jwtAuthenticationConverter(SecurityProperties props) {
        var jwtAuthConverter = new ReactiveJwtAuthenticationConverter();
        jwtAuthConverter.setJwtGrantedAuthoritiesConverter(
                new ReactiveJwtGrantedAuthoritiesConverterAdapter(new KeycloakPermissionsConverter()));
        jwtAuthConverter.setPrincipalClaimName(props.getPrincipalClaim());
        return jwtAuthConverter;
    }

    @Bean
    @ConditionalOnMissingBean
        public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http,
            Converter<Jwt, Mono<AbstractAuthenticationToken>> jwtConverter,
            SecurityProperties props) {
        return http
            .authorizeExchange(exchanges -> exchanges
                .pathMatchers(props.getPublicPaths().toArray(String[]::new)).permitAll()
                .anyExchange().authenticated())
            .oauth2ResourceServer(oauth2 -> oauth2.jwt(jwt -> jwt.jwtAuthenticationConverter(jwtConverter)))
            .build();
        }
}

@ConfigurationProperties(prefix = "security")
class SecurityProperties {
    /** Public (permitAll) path patterns. */
    private List<String> publicPaths = List.of(
            "/actuator/**",
            "/swagger-ui.html",
            "/swagger-ui/**",
            "/v3/api-docs",
            "/v3/api-docs/**");

    /** JWT principal claim name (default 'sub'). */
    private String principalClaim = "sub";

    public List<String> getPublicPaths() { return publicPaths; }
    public void setPublicPaths(List<String> publicPaths) { this.publicPaths = publicPaths; }
    public String getPrincipalClaim() { return principalClaim; }
    public void setPrincipalClaim(String principalClaim) { this.principalClaim = principalClaim; }
}
