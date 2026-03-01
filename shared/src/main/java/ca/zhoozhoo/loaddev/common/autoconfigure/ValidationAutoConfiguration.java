package ca.zhoozhoo.loaddev.common.autoconfigure;

import org.hibernate.validator.HibernateValidator;
import org.hibernate.validator.HibernateValidatorConfiguration;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Bean;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

import ca.zhoozhoo.loaddev.common.validation.NotNullQuantityValidator;
import ca.zhoozhoo.loaddev.common.validation.PositiveQuantityValidator;
import jakarta.validation.Validation;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

/// Auto-configuration for Bean Validation with JSR-385 Quantity support.
///
/// Registers [PositiveQuantityValidator] and [NotNullQuantityValidator]
/// for `@Positive` and `@NotNull` annotations on Quantity types.
/// Runs before Spring Boot's default validation auto-configuration to ensure
/// custom Quantity validators are registered.
///
/// @author Zhubin Salehi
@AutoConfiguration(beforeName = "org.springframework.boot.validation.autoconfigure.ValidationAutoConfiguration")
@ConditionalOnClass(HibernateValidator.class)
public class ValidationAutoConfiguration {

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
