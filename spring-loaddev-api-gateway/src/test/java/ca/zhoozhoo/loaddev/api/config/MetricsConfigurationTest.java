package ca.zhoozhoo.loaddev.api.config;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import io.micrometer.core.instrument.MeterRegistry;
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
    @DisplayName("Should use OpenTelemetryMeterRegistry as primary")
    void shouldUseOpenTelemetryMeterRegistryAsPrimary() {
    assertThat(meterRegistry)
        .isInstanceOf(OpenTelemetryMeterRegistry.class);
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
