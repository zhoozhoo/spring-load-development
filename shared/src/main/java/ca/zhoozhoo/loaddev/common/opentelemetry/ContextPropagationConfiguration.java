package ca.zhoozhoo.loaddev.common.opentelemetry;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.support.ContextPropagatingTaskDecorator;

/// Configuration for propagating context across thread boundaries in reactive and async operations.
///
/// This configuration is critical for maintaining observability context (trace IDs, span IDs,
/// baggage) when operations cross thread boundaries, which is common in reactive programming
/// with Project Reactor and asynchronous task execution.
///
/// **Context Propagation Mechanism:**
///
/// The [ContextPropagatingTaskDecorator] wraps async tasks to ensure that Spring's
/// ThreadLocal-based context (including Micrometer's observation context) is copied to worker
/// threads. This is essential for:
///
/// - **Trace Continuity:** Maintaining the same trace ID across async operations, enabling
/// end-to-end request tracking even when execution switches threads
/// - **MDC Propagation:** Ensuring Log4j2's Mapped Diagnostic Context (trace IDs, span IDs)
/// is available in log statements from worker threads
/// - **Baggage Propagation:** Preserving OpenTelemetry baggage (custom context data) across
/// thread boundaries for distributed context
///
/// **Integration with OpenTelemetry:**
///
/// Works in conjunction with [OpenTelemetryConfiguration] to ensure that:
///
/// - Incoming requests establish a trace context (traceparent header)
/// - Reactive operators (flatMap, publishOn, subscribeOn) preserve the context
/// - Async tasks (@Async, ExecutorService) receive the propagated context
/// - Outgoing HTTP calls inherit the trace context for distributed tracing
///
/// **Spring Boot 4.0 / Spring Framework 7.0 Integration:**
///
/// Leverages Spring Framework 7.0's enhanced `ContextPropagatingTaskDecorator` which
/// integrates with Micrometer's `ObservationRegistry` and OpenTelemetry's context propagation.
///
/// @author Zhubin Salehi
/// @see ContextPropagatingTaskDecorator
/// @see OpenTelemetryConfiguration
/// @see <a href="https://opentelemetry.io/docs/instrumentation/java/manual/#context-propagation">OpenTelemetry Context Propagation</a>
@Configuration(proxyBeanMethods = false)
public class ContextPropagationConfiguration {

    /// Creates a task decorator that propagates context across thread boundaries.
    ///
    /// This decorator is automatically applied to:
    ///
    /// - Spring's `@Async` methods
    /// - TaskExecutor instances
    /// - Scheduled tasks (@Scheduled)
    /// - Custom async operations using Spring's task abstraction
    ///
    /// The decorator captures the current context before task execution and restores it
    /// in the worker thread, ensuring trace continuity and proper log correlation.
    ///
    /// @return a ContextPropagatingTaskDecorator for automatic context propagation
    @Bean
    ContextPropagatingTaskDecorator contextPropagatingTaskDecorator() {
        return new ContextPropagatingTaskDecorator();
    }

}