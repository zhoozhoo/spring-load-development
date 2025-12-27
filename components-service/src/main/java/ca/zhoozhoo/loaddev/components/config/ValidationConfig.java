package ca.zhoozhoo.loaddev.components.config;

import org.hibernate.validator.HibernateValidator;
import org.hibernate.validator.HibernateValidatorConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

import ca.zhoozhoo.loaddev.common.validation.NotNullQuantityValidator;
import ca.zhoozhoo.loaddev.common.validation.PositiveQuantityValidator;
import jakarta.validation.Validation;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

/**
 * Bean Validation configuration registering custom Quantity validators.
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
                .constraintDefinition(Positive.class)
                .includeExistingValidators(true)
                .validatedBy(PositiveQuantityValidator.class);

        // Register NotNullQuantityValidator for Quantity types
        constraintMapping
                .constraintDefinition(NotNull.class)
                .includeExistingValidators(true)
                .validatedBy(NotNullQuantityValidator.class);

        configuration.addMapping(constraintMapping);

        validatorFactoryBean.setConfigurationInitializer(
                cfg -> ((HibernateValidatorConfiguration) cfg).addMapping(constraintMapping));

        return validatorFactoryBean;
    }
}
