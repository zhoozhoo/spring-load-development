package ca.zhoozhoo.loaddev.common.opentelemetry;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.http.server.reactive.observation.DefaultServerRequestObservationConvention;

import io.micrometer.core.instrument.binder.jvm.ClassLoaderMetrics;
import io.micrometer.core.instrument.binder.jvm.JvmMemoryMetrics;
import io.micrometer.core.instrument.binder.jvm.JvmThreadMetrics;
import io.micrometer.core.instrument.binder.jvm.convention.otel.OpenTelemetryJvmCpuMeterConventions;
import io.micrometer.core.instrument.binder.system.ProcessorMetrics;
import io.opentelemetry.api.OpenTelemetry;

class OpenTelemetryConfigurationTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(OpenTelemetryConfiguration.class))
            .withBean(OpenTelemetry.class, () -> mock(OpenTelemetry.class));

    @Test
    void providesInstallOpenTelemetryAppenderBean() {
        contextRunner.run(ctx -> {
            assertThat(ctx).hasSingleBean(InstallOpenTelemetryAppender.class);
        });
    }

    @Test
    void providesDefaultServerRequestObservationConventionBean() {
        contextRunner.run(ctx -> {
            assertThat(ctx).hasSingleBean(DefaultServerRequestObservationConvention.class);
        });
    }

    @Test
    void providesOpenTelemetryJvmCpuMeterConventionsBean() {
        contextRunner.run(ctx -> {
            assertThat(ctx).hasSingleBean(OpenTelemetryJvmCpuMeterConventions.class);
        });
    }

    @Test
    void providesProcessorMetricsBean() {
        contextRunner.run(ctx -> {
            assertThat(ctx).hasSingleBean(ProcessorMetrics.class);
        });
    }

    @Test
    void providesJvmMemoryMetricsBean() {
        contextRunner.run(ctx -> {
            assertThat(ctx).hasSingleBean(JvmMemoryMetrics.class);
        });
    }

    @Test
    void providesJvmThreadMetricsBean() {
        contextRunner.run(ctx -> {
            assertThat(ctx).hasSingleBean(JvmThreadMetrics.class);
        });
    }

    @Test
    void providesClassLoaderMetricsBean() {
        contextRunner.run(ctx -> {
            assertThat(ctx).hasSingleBean(ClassLoaderMetrics.class);
        });
    }

    @Test
    void installOpenTelemetryAppenderBeanIsNotNull() {
        contextRunner.run(ctx -> assertThat(ctx.getBean(InstallOpenTelemetryAppender.class)).isNotNull());
    }

    @Test
    void allMetricsBeansAreNotNull() {
        contextRunner.run(ctx -> {
            assertThat(ctx.getBean(ProcessorMetrics.class)).isNotNull();
            assertThat(ctx.getBean(JvmMemoryMetrics.class)).isNotNull();
            assertThat(ctx.getBean(JvmThreadMetrics.class)).isNotNull();
            assertThat(ctx.getBean(ClassLoaderMetrics.class)).isNotNull();
        });
    }

    @Test
    void openTelemetryJvmCpuMeterConventionsBeanIsConfiguredCorrectly() {
        contextRunner.run(ctx -> assertThat(ctx.getBean(OpenTelemetryJvmCpuMeterConventions.class)).isNotNull());
    }

    @Test
    void defaultServerRequestObservationConventionBeanIsConfiguredCorrectly() {
        contextRunner.run(ctx -> assertThat(ctx.getBean(DefaultServerRequestObservationConvention.class)).isNotNull());
    }
}
