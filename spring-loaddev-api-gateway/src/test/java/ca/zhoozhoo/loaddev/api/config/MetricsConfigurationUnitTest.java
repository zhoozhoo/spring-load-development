package ca.zhoozhoo.loaddev.api.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verifyNoInteractions;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import io.micrometer.core.instrument.Clock;
import io.micrometer.core.instrument.composite.CompositeMeterRegistry;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import io.opentelemetry.api.OpenTelemetry;

/**
 * Unit tests for {@link MetricsConfiguration}.
 * Tests edge cases and error handling with mocked dependencies.
 * 
 * @author Zhubin Salehi
 */
@ExtendWith(MockitoExtension.class)
class MetricsConfigurationUnitTest {

    @Mock
    private OpenTelemetry openTelemetry;

    @Mock
    private Clock clock;

    @InjectMocks
    private MetricsConfiguration configuration;

    @Test
    @DisplayName("Should skip setup when OpenTelemetry is null")
    void shouldSkipSetupWhenOpenTelemetryIsNull() {
        // Given
        ReflectionTestUtils.setField(configuration, "openTelemetry", null);
        var composite = new CompositeMeterRegistry();
        ReflectionTestUtils.setField(configuration, "meterRegistry", composite);
        ReflectionTestUtils.setField(configuration, "clock", clock);

        int initialSize = composite.getRegistries().size();

        // When
        configuration.setupOpenTelemetryRegistry();

        // Then - no registry should be added
        assertThat(composite.getRegistries()).hasSize(initialSize);
    }

    @Test
    @DisplayName("Should skip setup when MeterRegistry is not CompositeMeterRegistry")
    void shouldSkipSetupWhenNotCompositeMeterRegistry() {
        // Given
        var simpleMeterRegistry = new SimpleMeterRegistry();
        ReflectionTestUtils.setField(configuration, "openTelemetry", openTelemetry);
        ReflectionTestUtils.setField(configuration, "meterRegistry", simpleMeterRegistry);
        ReflectionTestUtils.setField(configuration, "clock", clock);

        // When
        configuration.setupOpenTelemetryRegistry();

        // Then - OpenTelemetry should not be used
        verifyNoInteractions(openTelemetry);
    }
}
