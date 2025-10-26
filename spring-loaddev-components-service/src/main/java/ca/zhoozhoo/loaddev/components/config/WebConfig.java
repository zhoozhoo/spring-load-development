package ca.zhoozhoo.loaddev.components.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.lang.NonNull;
import org.springframework.web.reactive.config.WebFluxConfigurer;
import org.springframework.web.reactive.result.method.annotation.ArgumentResolverConfigurer;

import ca.zhoozhoo.loaddev.components.security.CurrentUserMethodArgumentResolver;

/**
 * WebFlux configuration for the components service.
 * <p>
 * Configures custom argument resolvers for reactive web endpoints,
 * specifically enabling the {@link CurrentUserMethodArgumentResolver}
 * for automatic injection of the current user's ID from security context.
 * </p>
 *
 * @author Zhubin Salehi
 */
@Configuration
public class WebConfig implements WebFluxConfigurer {

    @Override
    public void configureArgumentResolvers(@NonNull ArgumentResolverConfigurer configurer) {
        configurer.addCustomResolver(new CurrentUserMethodArgumentResolver());
    }
}
