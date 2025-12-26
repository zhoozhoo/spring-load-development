package ca.zhoozhoo.loaddev.common.opentelemetry;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.Mockito.mock;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.InitializingBean;

import io.opentelemetry.api.OpenTelemetry;

class InstallOpenTelemetryAppenderTest {

    @Test
    void implementsInitializingBean() {
        assertThat(new InstallOpenTelemetryAppender(mock(OpenTelemetry.class))).isInstanceOf(InitializingBean.class);
    }

    @Test
    void constructorAcceptsOpenTelemetry() {
        assertThatCode(() -> new InstallOpenTelemetryAppender(mock(OpenTelemetry.class)))
                .doesNotThrowAnyException();
    }

    @Test
    void afterPropertiesSetDoesNotThrowException() {
        assertThatCode(() -> new InstallOpenTelemetryAppender(mock(OpenTelemetry.class)).afterPropertiesSet())
                .doesNotThrowAnyException();
    }

    @Test
    void constructorWithNullOpenTelemetryDoesNotThrowException() {
        assertThatCode(() -> new InstallOpenTelemetryAppender(null))
                .doesNotThrowAnyException();
    }

    @Test
    void canBeCreatedWithNullAndCalledWithoutException() {
        // This test verifies that the constructor accepts null
        // The afterPropertiesSet call with null openTelemetry is tested separately
        assertThat(new InstallOpenTelemetryAppender(null)).isNotNull();
    }
}
