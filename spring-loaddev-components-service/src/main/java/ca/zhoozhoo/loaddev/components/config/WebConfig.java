package ca.zhoozhoo.loaddev.components.config;

import org.jspecify.annotations.NonNull;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.config.WebFluxConfigurer;
import org.springframework.web.reactive.result.method.annotation.ArgumentResolverConfigurer;

import ca.zhoozhoo.loaddev.components.security.CurrentUserMethodArgumentResolver;

/**
 * WebFlux configuration registering {@link CurrentUserMethodArgumentResolver}.
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
