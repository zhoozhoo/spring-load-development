package ca.zhoozhoo.loaddev.common.opentelemetry;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.core.task.support.ContextPropagatingTaskDecorator;

class ContextPropagationConfigurationTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(ContextPropagationConfiguration.class));

    @Test
    void providesContextPropagatingTaskDecoratorBean() {
        contextRunner.run(ctx -> {
            assertThat(ctx).hasSingleBean(ContextPropagatingTaskDecorator.class);
        });
    }

    @Test
    void contextPropagatingTaskDecoratorBeanIsNotNull() {
        contextRunner.run(ctx -> assertThat(ctx.getBean(ContextPropagatingTaskDecorator.class)).isNotNull());
    }

    @Test
    void contextPropagatingTaskDecoratorCanDecorateRunnable() {
        contextRunner.run(ctx -> {
            assertThat(ctx.getBean(ContextPropagatingTaskDecorator.class).decorate(() -> System.out.println("test")))
                    .isNotNull();
        });
    }
}
