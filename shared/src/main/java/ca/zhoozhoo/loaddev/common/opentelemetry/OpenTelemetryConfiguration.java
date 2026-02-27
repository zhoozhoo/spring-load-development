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

/// Central configuration for OpenTelemetry observability integration across all microservices.
///
/// This configuration establishes comprehensive observability through the OpenTelemetry standard,
/// enabling unified metrics, traces, and logs collection. It bridges Spring Boot's native
/// observability features (Micrometer, Spring Boot Actuator) with OpenTelemetry's vendor-neutral
/// telemetry format.
///
/// **Key Observability Components:**
///
/// - **Distributed Tracing:** W3C Trace Context propagation across HTTP calls, reactive streams,
/// and async operations. Spans are automatically created for HTTP requests, database queries,
/// and custom operations annotated with `@Observed`.
/// - **Metrics Collection:** JVM metrics (memory, threads, GC, CPU), HTTP server metrics,
/// database connection pool metrics, and custom application metrics following OpenTelemetry
/// semantic conventions.
/// - **Structured Logging:** Log4j2 integration with automatic trace context injection
/// (trace_id, span_id) into MDC, enabling log correlation with distributed traces.
///
/// **OpenTelemetry Semantic Conventions:**
///
/// All metrics follow OpenTelemetry semantic conventions for consistent naming and attributes
/// across different programming languages and frameworks. This includes:
///
/// - JVM metrics: `process.runtime.jvm.*`, `process.cpu.*`
/// - HTTP metrics: `http.server.*`, `http.client.*`
/// - Database metrics: `db.*`
///
/// **Spring Boot 4.0 Integration:**
///
/// This configuration leverages Spring Boot 4.0's built-in OpenTelemetry auto-configuration
/// (spring-boot-starter-actuator-opentelemetry) and extends it with:
///
/// - OpenTelemetry-compliant JVM metrics using Micrometer bridges
/// - Log4j2 appender for automatic log trace correlation
/// - Custom observation conventions for reactive HTTP servers
///
/// **Exporter Configuration:**
///
/// OpenTelemetry SDK is configured via application properties to export telemetry data
/// to OTLP (OpenTelemetry Protocol) endpoints:
/// ```java
/// management.otlp.metrics.export.url=http://localhost:4318/v1/metrics
/// management.otlp.tracing.export.url=http://localhost:4318/v1/traces
/// management.otlp.logging.export.url=http://localhost:4318/v1/logs
/// ```
///
/// @author Zhubin Salehi
/// @see ContextPropagationConfiguration
/// @see InstallOpenTelemetryAppender
/// @see <a href="https://opentelemetry.io/docs/specs/semconv/">OpenTelemetry Semantic Conventions</a>
/// @see <a href="https://docs.spring.io/spring-boot/docs/4.0.x/reference/html/actuator.html#actuator.metrics.export.otlp">Spring Boot OpenTelemetry</a>
@Configuration(proxyBeanMethods = false)
public class OpenTelemetryConfiguration {

    /// Installs the OpenTelemetry Log4j2 appender for automatic log correlation.
    ///
    /// This bean initializes the Log4j2 OpenTelemetry appender during application startup,
    /// enabling automatic injection of trace context (trace_id, span_id) into log events.
    ///
    /// @param openTelemetry the auto-configured OpenTelemetry instance from Spring Boot
    /// @return an initializing bean that installs the Log4j2 appender
    /// @see InstallOpenTelemetryAppender
    @Bean
    InstallOpenTelemetryAppender installOpenTelemetryAppender(OpenTelemetry openTelemetry) {
        return new InstallOpenTelemetryAppender(openTelemetry);
    }

    /// Configures observation conventions for reactive HTTP server requests.
    ///
    /// This convention determines which attributes are added to HTTP server spans,
    /// following OpenTelemetry semantic conventions for HTTP servers:
    ///
    /// - `http.server.request.duration` - request duration in seconds
    /// - `http.route` - matched route pattern
    /// - `http.request.method` - HTTP method (GET, POST, etc.)
    /// - `http.response.status_code` - HTTP status code
    /// - `url.scheme`, `url.path`, `url.query` - URL components
    ///
    /// @return observation convention for WebFlux server requests
    /// @see DefaultServerRequestObservationConvention
    @Bean
    DefaultServerRequestObservationConvention openTelemetryServerRequestObservationConvention() {
        return new DefaultServerRequestObservationConvention();
    }

    /// Creates JVM CPU metrics convention following OpenTelemetry semantic conventions.
    ///
    /// Defines naming and attributes for CPU-related metrics:
    ///
    /// - `process.cpu.usage` - CPU usage percentage (0-1)
    /// - `process.cpu.time` - CPU time in seconds
    ///
    /// @return OpenTelemetry-compliant CPU meter conventions
    @Bean
    OpenTelemetryJvmCpuMeterConventions openTelemetryJvmCpuMeterConventions() {
        return new OpenTelemetryJvmCpuMeterConventions(Tags.empty());
    }

    /// Configures processor (CPU) metrics collection with OpenTelemetry conventions.
    ///
    /// Collects system-level CPU metrics:
    ///
    /// - `system.cpu.usage` - system CPU usage
    /// - `system.cpu.count` - number of available processors
    /// - `system.load.average.1m` - 1-minute load average (Unix-like systems)
    ///
    /// @return processor metrics binder with OpenTelemetry conventions
    @Bean
    ProcessorMetrics processorMetrics() {
        return new ProcessorMetrics(List.of(), new OpenTelemetryJvmCpuMeterConventions(Tags.empty()));
    }

    /// Configures JVM memory metrics with OpenTelemetry semantic conventions.
    ///
    /// Collects detailed memory metrics across heap, non-heap, and buffer pools:
    ///
    /// - `process.runtime.jvm.memory.usage` - current memory usage by area and pool
    /// - `process.runtime.jvm.memory.committed` - committed memory
    /// - `process.runtime.jvm.memory.limit` - maximum memory limit
    /// - `process.runtime.jvm.buffer.usage` - buffer pool usage (direct, mapped)
    ///
    /// Metrics are tagged with memory type (heap/non-heap) and pool name (Eden, Survivor, Old Gen, etc.).
    ///
    /// @return JVM memory metrics binder with OpenTelemetry conventions
    @Bean
    JvmMemoryMetrics jvmMemoryMetrics() {
        return new JvmMemoryMetrics(List.of(), new OpenTelemetryJvmMemoryMeterConventions(Tags.empty()));
    }

    /// Configures JVM thread metrics with OpenTelemetry semantic conventions.
    ///
    /// Collects thread-related metrics:
    ///
    /// - `process.runtime.jvm.threads.count` - current thread count by state
    /// (runnable, blocked, waiting, timed_waiting)
    /// - `process.runtime.jvm.threads.daemon` - daemon thread count
    /// - `process.runtime.jvm.threads.peak` - peak thread count
    ///
    /// @return JVM thread metrics binder with OpenTelemetry conventions
    @Bean
    JvmThreadMetrics jvmThreadMetrics() {
        return new JvmThreadMetrics(List.of(), new OpenTelemetryJvmThreadMeterConventions(Tags.empty()));
    }

    /// Configures JVM class loading metrics with OpenTelemetry semantic conventions.
    ///
    /// Collects metrics about class loading activity:
    ///
    /// - `process.runtime.jvm.classes.loaded` - number of classes currently loaded
    /// - `process.runtime.jvm.classes.unloaded` - total classes unloaded since JVM start
    /// - `process.runtime.jvm.classes.current_loaded` - current loaded class count
    ///
    /// @return class loader metrics binder with OpenTelemetry conventions
    @Bean
    ClassLoaderMetrics classLoaderMetrics() {
        return new ClassLoaderMetrics(new OpenTelemetryJvmClassLoadingMeterConventions());
    }
}
