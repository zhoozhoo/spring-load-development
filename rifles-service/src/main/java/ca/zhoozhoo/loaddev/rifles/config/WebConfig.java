package ca.zhoozhoo.loaddev.rifles.config;

import org.jspecify.annotations.NonNull;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.config.WebFluxConfigurer;
import org.springframework.web.reactive.result.method.annotation.ArgumentResolverConfigurer;

import ca.zhoozhoo.loaddev.security.CurrentUserMethodArgumentResolver;

/**
 * WebFlux configuration for custom argument resolvers.
 * <p>
 * Registers {@link CurrentUserMethodArgumentResolver} to inject authenticated user IDs
 * into controller method parameters annotated with {@code @CurrentUser}.
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
