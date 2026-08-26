package ca.zhoozhoo.loaddev.rifles.config;

import org.jspecify.annotations.NonNull;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.method.HandlerTypePredicate;
import org.springframework.web.reactive.config.ApiVersionConfigurer;
import org.springframework.web.reactive.config.PathMatchConfigurer;
import org.springframework.web.reactive.config.WebFluxConfigurer;

@Configuration(proxyBeanMethods = false)
public class ApiVersionConfiguration implements WebFluxConfigurer {

    @Override
    public void configureApiVersioning(@NonNull ApiVersionConfigurer configurer) {
        configurer.usePathSegment(0)
                .setDefaultVersion("1");
    }

    @Override
    public void configurePathMatching(@NonNull PathMatchConfigurer configurer) {
        configurer.addPathPrefix("/v{version}", HandlerTypePredicate.forAnnotation(RestController.class));
    }
}