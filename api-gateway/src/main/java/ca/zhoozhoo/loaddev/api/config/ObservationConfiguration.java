package ca.zhoozhoo.loaddev.api.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.micrometer.observation.ObservationPredicate;

/**
 * Observation-related tweaks for the API Gateway.
 *
 * <p>Reactor context propagation is enabled via {@code spring.reactor.context-propagation=AUTO}
 * (see config in the config repo). This class only disables the (very noisy) Spring Security
 * filter-chain observation which is known to produce confusing scope warnings in reactive stacks
 * and generally doesn't add much value compared to HTTP server/client observations.</p>
 *
 * @author Zhubin Salehi
 */
@Configuration(proxyBeanMethods = false)
public class ObservationConfiguration {
    /**
     * Disables the problematic spring.security.filterchains observation.
     * 
     * <p>The Spring Security filter chain observation creates scopes that don't properly
     * work with reactive context propagation, causing warnings about observation scope mismatches.
     * Since this observation doesn't provide critical value and causes issues, we disable it.</p>
     * 
     * @return an observation predicate that filters out the spring.security.filterchains observation
     */
    @Bean
    public ObservationPredicate disableSpringSecurityFilterchainsObservation() {
        return (name, context) -> !"spring.security.filterchains".equals(name);
    }
}
