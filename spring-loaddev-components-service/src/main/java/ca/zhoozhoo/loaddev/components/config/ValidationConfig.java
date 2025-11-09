package ca.zhoozhoo.loaddev.components.config;

import org.hibernate.validator.HibernateValidator;
import org.hibernate.validator.HibernateValidatorConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

import ca.zhoozhoo.loaddev.components.validation.PositiveQuantityValidator;
import jakarta.validation.Validation;
import jakarta.validation.constraints.Positive;

/**
 * Configuration for custom Bean Validation validators.
 * <p>
 * Registers custom validators for JSR-385 Quantity types to enable
 * Bean Validation annotations like @Positive to work with
 * javax.measure.Quantity fields.
 * </p>
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

        constraintMapping
                .constraintDefinition(Positive.class)
                .includeExistingValidators(true)
                .validatedBy(PositiveQuantityValidator.class);

        configuration.addMapping(constraintMapping);
        validatorFactoryBean.setConfigurationInitializer(cfg -> {
            var hvConfig = (HibernateValidatorConfiguration) cfg;
            hvConfig.addMapping(constraintMapping);
        });

        return validatorFactoryBean;
    }
}
