package ca.zhoozhoo.loaddev.components.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.lang.NonNull;
import org.springframework.web.reactive.config.WebFluxConfigurer;
import org.springframework.web.reactive.result.method.annotation.ArgumentResolverConfigurer;

import ca.zhoozhoo.loaddev.components.security.CurrentUserMethodArgumentResolver;

@Configuration
public class WebConfig implements WebFluxConfigurer {

    @Override
    public void configureArgumentResolvers(@NonNull ArgumentResolverConfigurer configurer) {
        configurer.addCustomResolver(new CurrentUserMethodArgumentResolver());
    }
}
