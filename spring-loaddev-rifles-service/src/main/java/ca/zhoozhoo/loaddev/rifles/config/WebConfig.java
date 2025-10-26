package ca.zhoozhoo.loaddev.rifles.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.lang.NonNull;
import org.springframework.web.reactive.config.WebFluxConfigurer;
import org.springframework.web.reactive.result.method.annotation.ArgumentResolverConfigurer;

import ca.zhoozhoo.loaddev.rifles.security.CurrentUserMethodArgumentResolver;

/**
 * Web configuration for the rifles service.
 * <p>
 * Configures WebFlux custom argument resolvers, specifically registering the
 * {@link CurrentUserMethodArgumentResolver} to enable automatic injection of
 * the current authenticated user ID into controller method parameters annotated
 * with {@link ca.zhoozhoo.loaddev.rifles.security.CurrentUser}.
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
