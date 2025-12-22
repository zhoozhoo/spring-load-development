package ca.zhoozhoo.loaddev.common.opentelemetry;

import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.server.reactive.observation.DefaultServerRequestObservationConvention;

import io.micrometer.core.instrument.Tags;
import io.micrometer.core.instrument.binder.jvm.ClassLoaderMetrics;
import io.micrometer.core.instrument.binder.jvm.JvmMemoryMetrics;
import io.micrometer.core.instrument.binder.jvm.JvmThreadMetrics;
import io.micrometer.core.instrument.binder.jvm.convention.otel.OpenTelemetryJvmClassLoadingMeterConventions;
import io.micrometer.core.instrument.binder.jvm.convention.otel.OpenTelemetryJvmCpuMeterConventions;
import io.micrometer.core.instrument.binder.jvm.convention.otel.OpenTelemetryJvmMemoryMeterConventions;
import io.micrometer.core.instrument.binder.jvm.convention.otel.OpenTelemetryJvmThreadMeterConventions;
import io.micrometer.core.instrument.binder.system.ProcessorMetrics;
import io.opentelemetry.api.OpenTelemetry;

/**
 * Central configuration for OpenTelemetry observability integration across all microservices.
 * 
 * <p>This configuration establishes comprehensive observability through the OpenTelemetry standard,
 * enabling unified metrics, traces, and logs collection. It bridges Spring Boot's native
 * observability features (Micrometer, Spring Boot Actuator) with OpenTelemetry's vendor-neutral
 * telemetry format.</p>
 * 
 * <p><b>Key Observability Components:</b></p>
 * <ul>
 *   <li><b>Distributed Tracing:</b> W3C Trace Context propagation across HTTP calls, reactive streams,
 *       and async operations. Spans are automatically created for HTTP requests, database queries,
 *       and custom operations annotated with {@code @Observed}.</li>
 *   <li><b>Metrics Collection:</b> JVM metrics (memory, threads, GC, CPU), HTTP server metrics,
 *       database connection pool metrics, and custom application metrics following OpenTelemetry
 *       semantic conventions.</li>
 *   <li><b>Structured Logging:</b> Log4j2 integration with automatic trace context injection
 *       (trace_id, span_id) into MDC, enabling log correlation with distributed traces.</li>
 * </ul>
 * 
 * <p><b>OpenTelemetry Semantic Conventions:</b></p>
 * <p>All metrics follow OpenTelemetry semantic conventions for consistent naming and attributes
 * across different programming languages and frameworks. This includes:</p>
 * <ul>
 *   <li>JVM metrics: {@code process.runtime.jvm.*}, {@code process.cpu.*}</li>
 *   <li>HTTP metrics: {@code http.server.*}, {@code http.client.*}</li>
 *   <li>Database metrics: {@code db.*}</li>
 * </ul>
 * 
 * <p><b>Spring Boot 4.0 Integration:</b></p>
 * <p>This configuration leverages Spring Boot 4.0's built-in OpenTelemetry auto-configuration
 * (spring-boot-starter-actuator-opentelemetry) and extends it with:</p>
 * <ul>
 *   <li>OpenTelemetry-compliant JVM metrics using Micrometer bridges</li>
 *   <li>Log4j2 appender for automatic log trace correlation</li>
 *   <li>Custom observation conventions for reactive HTTP servers</li>
 * </ul>
 * 
 * <p><b>Exporter Configuration:</b></p>
 * <p>OpenTelemetry SDK is configured via application properties to export telemetry data
 * to OTLP (OpenTelemetry Protocol) endpoints:</p>
 * <pre>{@code
 * management.otlp.metrics.export.url=http://localhost:4318/v1/metrics
 * management.otlp.tracing.export.url=http://localhost:4318/v1/traces
 * management.otlp.logging.export.url=http://localhost:4318/v1/logs
 * }</pre>
 * 
 * @author Zhubin Salehi
 * @see ContextPropagationConfiguration
 * @see InstallOpenTelemetryAppender
 * @see <a href="https://opentelemetry.io/docs/specs/semconv/">OpenTelemetry Semantic Conventions</a>
 * @see <a href="https://docs.spring.io/spring-boot/docs/4.0.x/reference/html/actuator.html#actuator.metrics.export.otlp">Spring Boot OpenTelemetry</a>
 */
@Configuration(proxyBeanMethods = false)
public class OpenTelemetryConfiguration {

    /**
     * Installs the OpenTelemetry Log4j2 appender for automatic log correlation.
     * 
     * <p>This bean initializes the Log4j2 OpenTelemetry appender during application startup,
     * enabling automatic injection of trace context (trace_id, span_id) into log events.</p>
     * 
     * @param openTelemetry the auto-configured OpenTelemetry instance from Spring Boot
     * @return an initializing bean that installs the Log4j2 appender
     * @see InstallOpenTelemetryAppender
     */
    @Bean
    InstallOpenTelemetryAppender installOpenTelemetryAppender(OpenTelemetry openTelemetry) {
        return new InstallOpenTelemetryAppender(openTelemetry);
    }

    /**
     * Configures observation conventions for reactive HTTP server requests.
     * 
     * <p>This convention determines which attributes are added to HTTP server spans,
     * following OpenTelemetry semantic conventions for HTTP servers:</p>
     * <ul>
     *   <li>{@code http.server.request.duration} - request duration in seconds</li>
     *   <li>{@code http.route} - matched route pattern</li>
     *   <li>{@code http.request.method} - HTTP method (GET, POST, etc.)</li>
     *   <li>{@code http.response.status_code} - HTTP status code</li>
     *   <li>{@code url.scheme}, {@code url.path}, {@code url.query} - URL components</li>
     * </ul>
     * 
     * @return observation convention for WebFlux server requests
     * @see DefaultServerRequestObservationConvention
     */
    @Bean
    DefaultServerRequestObservationConvention openTelemetryServerRequestObservationConvention() {
        return new DefaultServerRequestObservationConvention();
    }

    /**
     * Creates JVM CPU metrics convention following OpenTelemetry semantic conventions.
     * 
     * <p>Defines naming and attributes for CPU-related metrics:</p>
     * <ul>
     *   <li>{@code process.cpu.usage} - CPU usage percentage (0-1)</li>
     *   <li>{@code process.cpu.time} - CPU time in seconds</li>
     * </ul>
     * 
     * @return OpenTelemetry-compliant CPU meter conventions
     */
    @Bean
    OpenTelemetryJvmCpuMeterConventions openTelemetryJvmCpuMeterConventions() {
        return new OpenTelemetryJvmCpuMeterConventions(Tags.empty());
    }

    /**
     * Configures processor (CPU) metrics collection with OpenTelemetry conventions.
     * 
     * <p>Collects system-level CPU metrics:</p>
     * <ul>
     *   <li>{@code system.cpu.usage} - system CPU usage</li>
     *   <li>{@code system.cpu.count} - number of available processors</li>
     *   <li>{@code system.load.average.1m} - 1-minute load average (Unix-like systems)</li>
     * </ul>
     * 
     * @return processor metrics binder with OpenTelemetry conventions
     */
    @Bean
    ProcessorMetrics processorMetrics() {
        return new ProcessorMetrics(List.of(), new OpenTelemetryJvmCpuMeterConventions(Tags.empty()));
    }

    /**
     * Configures JVM memory metrics with OpenTelemetry semantic conventions.
     * 
     * <p>Collects detailed memory metrics across heap, non-heap, and buffer pools:</p>
     * <ul>
     *   <li>{@code process.runtime.jvm.memory.usage} - current memory usage by area and pool</li>
     *   <li>{@code process.runtime.jvm.memory.committed} - committed memory</li>
     *   <li>{@code process.runtime.jvm.memory.limit} - maximum memory limit</li>
     *   <li>{@code process.runtime.jvm.buffer.usage} - buffer pool usage (direct, mapped)</li>
     * </ul>
     * 
     * <p>Metrics are tagged with memory type (heap/non-heap) and pool name (Eden, Survivor, Old Gen, etc.).</p>
     * 
     * @return JVM memory metrics binder with OpenTelemetry conventions
     */
    @Bean
    JvmMemoryMetrics jvmMemoryMetrics() {
        return new JvmMemoryMetrics(List.of(), new OpenTelemetryJvmMemoryMeterConventions(Tags.empty()));
    }

    /**
     * Configures JVM thread metrics with OpenTelemetry semantic conventions.
     * 
     * <p>Collects thread-related metrics:</p>
     * <ul>
     *   <li>{@code process.runtime.jvm.threads.count} - current thread count by state
     *       (runnable, blocked, waiting, timed_waiting)</li>
     *   <li>{@code process.runtime.jvm.threads.daemon} - daemon thread count</li>
     *   <li>{@code process.runtime.jvm.threads.peak} - peak thread count</li>
     * </ul>
     * 
     * @return JVM thread metrics binder with OpenTelemetry conventions
     */
    @Bean
    JvmThreadMetrics jvmThreadMetrics() {
        return new JvmThreadMetrics(List.of(), new OpenTelemetryJvmThreadMeterConventions(Tags.empty()));
    }

    /**
     * Configures JVM class loading metrics with OpenTelemetry semantic conventions.
     * 
     * <p>Collects metrics about class loading activity:</p>
     * <ul>
     *   <li>{@code process.runtime.jvm.classes.loaded} - number of classes currently loaded</li>
     *   <li>{@code process.runtime.jvm.classes.unloaded} - total classes unloaded since JVM start</li>
     *   <li>{@code process.runtime.jvm.classes.current_loaded} - current loaded class count</li>
     * </ul>
     * 
     * @return class loader metrics binder with OpenTelemetry conventions
     */
    @Bean
    ClassLoaderMetrics classLoaderMetrics() {
        return new ClassLoaderMetrics(new OpenTelemetryJvmClassLoadingMeterConventions());
    }
}
