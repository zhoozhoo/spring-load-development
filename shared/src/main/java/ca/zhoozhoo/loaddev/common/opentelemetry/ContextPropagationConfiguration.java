package ca.zhoozhoo.loaddev.common.opentelemetry;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.support.ContextPropagatingTaskDecorator;

/**
 * Configuration for propagating context across thread boundaries in reactive and async operations.
 * 
 * <p>This configuration is critical for maintaining observability context (trace IDs, span IDs,
 * baggage) when operations cross thread boundaries, which is common in reactive programming
 * with Project Reactor and asynchronous task execution.</p>
 * 
 * <p><b>Context Propagation Mechanism:</b></p>
 * <p>The {@link ContextPropagatingTaskDecorator} wraps async tasks to ensure that Spring's
 * ThreadLocal-based context (including Micrometer's observation context) is copied to worker
 * threads. This is essential for:</p>
 * <ul>
 *   <li><b>Trace Continuity:</b> Maintaining the same trace ID across async operations, enabling
 *       end-to-end request tracking even when execution switches threads</li>
 *   <li><b>MDC Propagation:</b> Ensuring Log4j2's Mapped Diagnostic Context (trace IDs, span IDs)
 *       is available in log statements from worker threads</li>
 *   <li><b>Baggage Propagation:</b> Preserving OpenTelemetry baggage (custom context data) across
 *       thread boundaries for distributed context</li>
 * </ul>
 * 
 * <p><b>Integration with OpenTelemetry:</b></p>
 * <p>Works in conjunction with {@link OpenTelemetryConfiguration} to ensure that:</p>
 * <ol>
 *   <li>Incoming requests establish a trace context (traceparent header)</li>
 *   <li>Reactive operators (flatMap, publishOn, subscribeOn) preserve the context</li>
 *   <li>Async tasks (@Async, ExecutorService) receive the propagated context</li>
 *   <li>Outgoing HTTP calls inherit the trace context for distributed tracing</li>
 * </ol>
 * 
 * <p><b>Spring Boot 4.0 / Spring Framework 7.0 Integration:</b></p>
 * <p>Leverages Spring Framework 7.0's enhanced {@code ContextPropagatingTaskDecorator} which
 * integrates with Micrometer's {@code ObservationRegistry} and OpenTelemetry's context propagation.</p>
 * 
 * @author Zhubin Salehi
 * @see ContextPropagatingTaskDecorator
 * @see OpenTelemetryConfiguration
 * @see <a href="https://opentelemetry.io/docs/instrumentation/java/manual/#context-propagation">OpenTelemetry Context Propagation</a>
 */
@Configuration(proxyBeanMethods = false)
public class ContextPropagationConfiguration {

    /**
     * Creates a task decorator that propagates context across thread boundaries.
     * 
     * <p>This decorator is automatically applied to:</p>
     * <ul>
     *   <li>Spring's {@code @Async} methods</li>
     *   <li>TaskExecutor instances</li>
     *   <li>Scheduled tasks (@Scheduled)</li>
     *   <li>Custom async operations using Spring's task abstraction</li>
     * </ul>
     * 
     * <p>The decorator captures the current context before task execution and restores it
     * in the worker thread, ensuring trace continuity and proper log correlation.</p>
     * 
     * @return a ContextPropagatingTaskDecorator for automatic context propagation
     */
    @Bean
    ContextPropagatingTaskDecorator contextPropagatingTaskDecorator() {
        return new ContextPropagatingTaskDecorator();
    }

}