package ca.zhoozhoo.loaddev.api.config;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Assumptions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.composite.CompositeMeterRegistry;
import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.instrumentation.micrometer.v1_5.OpenTelemetryMeterRegistry;

/**
 * Integration tests for {@link MetricsConfiguration}.
 * Tests that OpenTelemetry MeterRegistry is properly configured.
 * 
 * @author Zhubin Salehi
 */
@SpringBootTest
@ActiveProfiles("test")
class MetricsConfigurationTest {

    @Autowired
    private MeterRegistry meterRegistry;

    @Autowired(required = false)
    private OpenTelemetry openTelemetry; // Optional – absent in CI if OTEL autoconfig disabled

    @Autowired
    private MetricsConfiguration metricsConfiguration;

    @Test
    @DisplayName("Should create MetricsConfiguration bean")
    void shouldCreateMetricsConfigurationBean() {
        assertThat(metricsConfiguration)
                .isNotNull()
                .isInstanceOf(MetricsConfiguration.class);
    }

    @Test
    @DisplayName("Should have MeterRegistry configured")
    void shouldHaveMeterRegistryConfigured() {
        assertThat(meterRegistry)
                .isNotNull()
                .isInstanceOf(MeterRegistry.class);
    }

    @Test
    @DisplayName("Should include OpenTelemetryMeterRegistry when OpenTelemetry bean present")
    void shouldIncludeOpenTelemetryMeterRegistryWhenPresent() {
        Assumptions.assumeTrue(openTelemetry != null, "OpenTelemetry bean not available – skipping OTEL-specific assertions");

        if (meterRegistry instanceof OpenTelemetryMeterRegistry) {
            // Direct registry case
            assertThat(meterRegistry).isInstanceOf(OpenTelemetryMeterRegistry.class);
        } else if (meterRegistry instanceof CompositeMeterRegistry composite) {
            assertThat(composite.getRegistries())
                .anySatisfy(r -> assertThat(r).isInstanceOf(OpenTelemetryMeterRegistry.class));
        } else {
            // Unexpected fallback
            assertThat(meterRegistry).isInstanceOf(OpenTelemetryMeterRegistry.class);
        }
    }

    @Test
    @DisplayName("Should gracefully fall back when OpenTelemetry absent")
    void shouldFallBackWhenOpenTelemetryAbsent() {
        Assumptions.assumeTrue(openTelemetry == null, "OpenTelemetry bean present – skipping fallback assertions");
        // In absence of OTEL we expect a generic MeterRegistry (often Composite or Simple)
        assertThat(meterRegistry)
            .isNotNull()
            .isInstanceOf(MeterRegistry.class)
            .isNotInstanceOf(OpenTelemetryMeterRegistry.class);
    }

    @Test
    @DisplayName("Should be able to register metrics")
    void shouldBeAbleToRegisterMetrics() {
        var counter = meterRegistry.counter("test.counter");
        counter.increment();
        
        // OpenTelemetry MeterRegistry doesn't support reading measurements locally
        // It's designed to export metrics to a backend system
        assertThat(counter)
                .isNotNull()
                .extracting("id.name")
                .isEqualTo("test.counter");
    }
}
