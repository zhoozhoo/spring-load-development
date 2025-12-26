package ca.zhoozhoo.loaddev.common.autoconfigure;

import ca.zhoozhoo.loaddev.common.jackson.QuantityModule;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;

/**
 * Auto-configuration that provides a {@link QuantityModule} bean if one is not already defined.
 * Consolidates repeated Jackson configuration in services.
 */
@AutoConfiguration
public class QuantityModuleAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean(QuantityModule.class)
    public QuantityModule quantityModule() {
        return new QuantityModule();
    }
}
