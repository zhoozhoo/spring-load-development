package ca.zhoozhoo.loaddev.loads.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.lang.NonNull;
import org.springframework.web.reactive.config.WebFluxConfigurer;
import org.springframework.web.reactive.result.method.annotation.ArgumentResolverConfigurer;

import ca.zhoozhoo.loaddev.loads.security.CurrentUserMethodArgumentResolver;

/**
 * WebFlux configuration for customizing web layer behavior.
 * <p>
 * This configuration registers custom argument resolvers for reactive controller methods,
 * specifically the {@link CurrentUserMethodArgumentResolver} for extracting the current
 * authenticated user's ID from JWT tokens.
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
