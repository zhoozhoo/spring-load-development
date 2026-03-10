package ca.zhoozhoo.loaddev.common.autoconfigure;

import org.jspecify.annotations.NonNull;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.method.HandlerTypePredicate;
import org.springframework.web.reactive.config.ApiVersionConfigurer;
import org.springframework.web.reactive.config.PathMatchConfigurer;
import org.springframework.web.reactive.config.WebFluxConfigurer;

/// Auto-configuration that enables Spring Framework 7 native API versioning
/// for all reactive services using URL path prefix resolution.
///
/// Configures version resolution from the URL path prefix (e.g., {@code /v1/loads})
/// and sets the default API version to {@code 1} for requests without a version prefix.
/// The {@code /v{version}} prefix is automatically added to all {@link RestController}
/// endpoints via {@link PathMatchConfigurer#addPathPrefix}.
///
/// This auto-configuration is only activated when at least one {@link RestController}
/// bean is present, preventing interference with non-REST services (e.g., MCP SSE servers)
/// where path segment version parsing would reject non-versioned endpoints like {@code /sse}.
///
/// @author Zhubin Salehi
/// @see ApiVersionConfigurer
/// @see PathMatchConfigurer
@AutoConfiguration
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.REACTIVE)
@ConditionalOnClass(WebFluxConfigurer.class)
@ConditionalOnBean(annotation = RestController.class)
public class ApiVersionAutoConfiguration implements WebFluxConfigurer {

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
