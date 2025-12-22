package ca.zhoozhoo.loaddev.common.opentelemetry;

import org.springframework.beans.factory.InitializingBean;

import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.instrumentation.log4j.appender.v2_17.OpenTelemetryAppender;

/**
 * Spring bean that installs the OpenTelemetry Log4j2 appender for automatic log correlation.
 * 
 * <p>This initializing bean bridges OpenTelemetry distributed tracing with Log4j2 logging,
 * enabling automatic injection of trace context into every log event. When traces are active,
 * log entries are automatically enriched with trace_id and span_id, allowing correlation
 * between logs and distributed traces.</p>
 * 
 * <p><b>Log Correlation Mechanism:</b></p>
 * <p>The OpenTelemetry Log4j2 appender intercepts log events and injects the current trace
 * context into the Mapped Diagnostic Context (MDC). This allows log patterns to include:</p>
 * <pre>{@code
 * <Pattern>%d{ISO8601} [%X{trace_id},%X{span_id}] %-5level [%t] %c{1.} - %msg%n</Pattern>
 * }</pre>
 * 
 * <p><b>Benefits of Log Correlation:</b></p>
 * <ul>
 *   <li><b>Distributed Debugging:</b> Trace a request across multiple microservices by filtering
 *       logs by trace_id, seeing the complete request flow in chronological order.</li>
 *   <li><b>Root Cause Analysis:</b> When a trace shows an error, immediately jump to the
 *       associated log entries with full stack traces and contextual information.</li>
 *   <li><b>Performance Investigation:</b> Correlate slow traces with application logs to
 *       identify exact operations causing latency (database queries, external API calls, etc.).</li>
 *   <li><b>Unified Observability:</b> Combine metrics, traces, and logs in a single view
 *       (e.g., Grafana dashboards) for comprehensive system understanding.</li>
 * </ul>
 * 
 * <p><b>Spring Bean Lifecycle:</b></p>
 * <p>This class implements {@link InitializingBean}, ensuring the appender is installed
 * during Spring's bean initialization phase, after dependency injection is complete but
 * before the application starts processing requests. The installation is a one-time operation
 * that configures Log4j2's global logging system.</p>
 * 
 * <p><b>OpenTelemetry Integration:</b></p>
 * <p>The appender uses the application's configured {@link OpenTelemetry} instance (provided
 * by Spring Boot's auto-configuration) to access the current trace context. It registers with
 * Log4j2's appender registry, making trace context available to all Log4j2 loggers throughout
 * the application without requiring code changes to logging statements.</p>
 * 
 * <p><b>Example Log Output:</b></p>
 * <pre>{@code
 * Without correlation:
 * 2024-01-15 10:30:45,123 INFO  [http-nio-8080-exec-1] c.z.l.service.LoadService - Processing load request
 * 
 * With correlation:
 * 2024-01-15 10:30:45,123 [a1b2c3d4e5f6g7h8,9i0j1k2l3m4n5o6p] INFO  [http-nio-8080-exec-1] c.z.l.service.LoadService - Processing load request
 * }</pre>
 * 
 * <p><b>Configuration Requirements:</b></p>
 * <p>The OpenTelemetry appender must be declared in Log4j2 configuration (log4j2.xml or log4j2.yaml):</p>
 * <pre>{@code
 * <Configuration>
 *   <Appenders>
 *     <OpenTelemetry name="OpenTelemetryAppender"/>
 *     <Console name="Console">
 *       <PatternLayout pattern="%d{ISO8601} [%X{trace_id},%X{span_id}] %-5level [%t] %c{1.} - %msg%n"/>
 *     </Console>
 *   </Appenders>
 *   <Loggers>
 *     <Root level="info">
 *       <AppenderRef ref="OpenTelemetryAppender"/>
 *       <AppenderRef ref="Console"/>
 *     </Root>
 *   </Loggers>
 * </Configuration>
 * }</pre>
 * 
 * @author Zhubin Salehi
 * @see OpenTelemetryConfiguration
 * @see OpenTelemetryAppender
 * @see <a href="https://opentelemetry.io/docs/instrumentation/java/manual/#logs">OpenTelemetry Java Logging</a>
 */
public class InstallOpenTelemetryAppender implements InitializingBean {

    private final OpenTelemetry openTelemetry;

    /**
     * Constructs an installer with the application's OpenTelemetry instance.
     * 
     * <p>The OpenTelemetry instance is injected by Spring Boot's auto-configuration,
     * configured via application properties (OTLP endpoints, resource attributes, etc.).</p>
     * 
     * @param openTelemetry the configured OpenTelemetry SDK instance
     */
    public InstallOpenTelemetryAppender(OpenTelemetry openTelemetry) {
        this.openTelemetry = openTelemetry;
    }

    /**
     * Installs the OpenTelemetry appender into Log4j2's global logging system.
     * 
     * <p>This method is invoked automatically by Spring after all bean properties are set,
     * during the application context initialization phase. It calls the static installation
     * method provided by the OpenTelemetry Log4j2 instrumentation library, which:</p>
     * <ol>
     *   <li>Registers a Log4j2 appender that intercepts all log events</li>
     *   <li>Extracts trace context from the current OpenTelemetry context</li>
     *   <li>Injects trace_id and span_id into Log4j2's ThreadContext (MDC)</li>
     *   <li>Makes trace context available to all downstream appenders and patterns</li>
     * </ol>
     * 
     * <p><b>Thread Safety:</b> The installation is idempotent and thread-safe. If called
     * multiple times, subsequent calls are ignored.</p>
     * 
     * @throws Exception if Log4j2 appender installation fails (propagated by Spring's
     *                   bean initialization error handling)
     */
    @Override
    public void afterPropertiesSet() {
        OpenTelemetryAppender.install(this.openTelemetry);
    }
}
