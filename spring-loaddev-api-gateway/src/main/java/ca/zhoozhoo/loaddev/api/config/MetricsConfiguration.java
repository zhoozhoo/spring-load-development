package ca.zhoozhoo.loaddev.api.config;

import io.micrometer.core.instrument.Clock;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.composite.CompositeMeterRegistry;
import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.instrumentation.micrometer.v1_5.OpenTelemetryMeterRegistry;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Configuration component that ensures OpenTelemetry MeterRegistry is created and registered
 * with the highest priority in the API Gateway's metrics system.
 * 
 * <p>This component explicitly creates an {@link OpenTelemetryMeterRegistry} and adds it to 
 * Spring Boot's {@link CompositeMeterRegistry} during application initialization. This ensures
 * that metrics are exported to OpenTelemetry rather than using a no-op or simple registry.</p>
 * 
 * <p>The setup runs during the {@link PostConstruct} phase, after all dependencies are injected
 * but before the application is fully started. This timing is critical to ensure the OpenTelemetry
 * registry is available when other components begin recording metrics.</p>
 * 
 * <p><b>Prerequisites:</b></p>
 * <ul>
 *   <li>OpenTelemetry SDK must be configured and available as a Spring bean</li>
 *   <li>Micrometer Clock bean must be available (provided by Spring Boot Actuator)</li>
 *   <li>application.yml must have: {@code otel.instrumentation.micrometer.enabled: true}</li>
 * </ul>
 * 
 * @author Zhubin Salehi
 * @see OpenTelemetryMeterRegistry
 * @see CompositeMeterRegistry
 */
@Component
public class MetricsConfiguration {

    private static final Logger log = LoggerFactory.getLogger(MetricsConfiguration.class);

    /**
     * OpenTelemetry SDK instance. Optional to gracefully handle cases where OpenTelemetry
     * is not configured.
     */
    @Autowired(required = false)
    private OpenTelemetry openTelemetry;

    /**
     * The primary MeterRegistry bean, typically a CompositeMeterRegistry that delegates
     * to all registered meter registries.
     */
    @Autowired
    private MeterRegistry meterRegistry;

    /**
     * Micrometer Clock for consistent time measurements across all metrics.
     */
    @Autowired
    private Clock clock;

    /**
     * Initializes and registers the OpenTelemetry MeterRegistry with the composite registry.
     * 
     * <p>This method:
     * <ol>
     *   <li>Verifies that OpenTelemetry SDK is available</li>
     *   <li>Confirms the primary MeterRegistry is a CompositeMeterRegistry</li>
     *   <li>Creates a new OpenTelemetryMeterRegistry instance</li>
     *   <li>Adds it to the composite registry</li>
     *   <li>Logs the final registry composition for verification</li>
     * </ol>
     * </p>
     * 
     * <p>If OpenTelemetry is not available, the setup is skipped with a warning,
     * allowing the application to start normally with alternative metrics exporters.</p>
     */
    @PostConstruct
    public void setupOpenTelemetryRegistry() {
        log.info("Setting up OpenTelemetry MeterRegistry");
        
        // Check if OpenTelemetry SDK is available
        if (openTelemetry == null) {
            log.warn("OpenTelemetry bean not found, skipping OpenTelemetryMeterRegistry setup");
            return;
        }

        // Verify we have a CompositeMeterRegistry to work with
        if (!(meterRegistry instanceof CompositeMeterRegistry composite)) {
            log.warn("MeterRegistry is not a CompositeMeterRegistry: {}", meterRegistry.getClass().getName());
            return;
        }
        
        // Create the OpenTelemetry MeterRegistry with the Micrometer clock
        log.info("Creating OpenTelemetryMeterRegistry");
        var otelRegistry = OpenTelemetryMeterRegistry.builder(openTelemetry)
                .setClock(clock)
                .build();
        
        // Add the registry to the composite so all metrics are exported to OpenTelemetry
        log.info("Adding OpenTelemetryMeterRegistry to CompositeMeterRegistry");
        composite.add(otelRegistry);
        
        // Log the final composition for verification
        log.info("CompositeMeterRegistry now contains {} registries", composite.getRegistries().size());
        composite.getRegistries().forEach(registry -> 
            log.info("  - Registry: {}", registry.getClass().getName())
        );
    }
}
