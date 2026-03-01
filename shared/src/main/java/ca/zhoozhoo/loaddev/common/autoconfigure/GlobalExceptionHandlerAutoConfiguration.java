package ca.zhoozhoo.loaddev.common.autoconfigure;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.reactive.config.WebFluxConfigurer;

import ca.zhoozhoo.loaddev.common.web.GlobalExceptionHandler;

/// Auto-configuration that registers the shared [GlobalExceptionHandler]
/// for centralized REST exception handling across all reactive services.
///
/// Only activates when spring-security and spring-tx are on the classpath,
/// since [GlobalExceptionHandler] references [AccessDeniedException] and
/// [DataIntegrityViolationException].
///
/// @author Zhubin Salehi
@AutoConfiguration
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.REACTIVE)
@ConditionalOnClass({WebFluxConfigurer.class, AccessDeniedException.class, DataIntegrityViolationException.class})
@Import(GlobalExceptionHandler.class)
public class GlobalExceptionHandlerAutoConfiguration {
}
