package ca.zhoozhoo.loaddev.loads.config;

import org.hibernate.validator.HibernateValidator;
import org.hibernate.validator.HibernateValidatorConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

import ca.zhoozhoo.loaddev.common.validation.NotNullQuantityValidator;
import ca.zhoozhoo.loaddev.common.validation.PositiveQuantityValidator;
import jakarta.validation.Validation;

/**
 * Bean Validation configuration for JSR-385 Quantity types.
 * <p>
 * Registers custom validators for @Positive and @NotNull annotations.
 *
 * @author Zhubin Salehi
 */
@Configuration
public class ValidationConfig {

    @Bean
    public LocalValidatorFactoryBean validator() {
        var validatorFactoryBean = new LocalValidatorFactoryBean();
        validatorFactoryBean.setProviderClass(HibernateValidator.class);

        var configuration = Validation
                .byProvider(HibernateValidator.class)
                .configure();

        var constraintMapping = configuration.createConstraintMapping();

        // Register PositiveQuantityValidator for Quantity types
        constraintMapping
                .constraintDefinition(jakarta.validation.constraints.Positive.class)
                .includeExistingValidators(true)
                .validatedBy(PositiveQuantityValidator.class);

        // Register NotNullQuantityValidator for Quantity types
        constraintMapping
                .constraintDefinition(jakarta.validation.constraints.NotNull.class)
                .includeExistingValidators(true)
                .validatedBy(NotNullQuantityValidator.class);

        configuration.addMapping(constraintMapping);
        
        validatorFactoryBean.setConfigurationInitializer(
                cfg -> ((HibernateValidatorConfiguration) cfg).addMapping(constraintMapping));

        return validatorFactoryBean;
    }
}
