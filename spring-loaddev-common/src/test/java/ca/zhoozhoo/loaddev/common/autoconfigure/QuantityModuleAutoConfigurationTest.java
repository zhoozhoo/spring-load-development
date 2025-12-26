package ca.zhoozhoo.loaddev.common.autoconfigure;

import static org.assertj.core.api.Assertions.assertThat;

import ca.zhoozhoo.loaddev.common.jackson.QuantityModule;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

class QuantityModuleAutoConfigurationTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(QuantityModuleAutoConfiguration.class));

    @Test
    void providesQuantityModuleBean() {
        contextRunner.run(ctx -> {
            assertThat(ctx).hasSingleBean(QuantityModule.class);
        });
    }
}
