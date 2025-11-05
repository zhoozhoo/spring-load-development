package ca.zhoozhoo.loaddev.loads.config;

import org.hibernate.validator.HibernateValidator;
import org.hibernate.validator.HibernateValidatorConfiguration;
import org.hibernate.validator.cfg.ConstraintMapping;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

import ca.zhoozhoo.loaddev.loads.validation.NotNullQuantityValidator;
import ca.zhoozhoo.loaddev.loads.validation.PositiveQuantityValidator;
import jakarta.validation.Validation;

/**
 * Configuration for custom Bean Validation validators.
 * <p>
 * Registers custom validators for JSR-385 Quantity types to enable
 * Bean Validation annotations like @Positive and @NotNull to work with
 * javax.measure.Quantity fields.
 * </p>
 *
 * @author Zhubin Salehi
 */
@Configuration
public class ValidationConfig {

    @Bean
    public LocalValidatorFactoryBean validator() {
        LocalValidatorFactoryBean validatorFactoryBean = new LocalValidatorFactoryBean();
        validatorFactoryBean.setProviderClass(HibernateValidator.class);
        
        HibernateValidatorConfiguration configuration = Validation
                .byProvider(HibernateValidator.class)
                .configure();

        ConstraintMapping constraintMapping = configuration.createConstraintMapping();

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
        validatorFactoryBean.setConfigurationInitializer(cfg -> {
            HibernateValidatorConfiguration hvConfig = (HibernateValidatorConfiguration) cfg;
            hvConfig.addMapping(constraintMapping);
        });
        
        return validatorFactoryBean;
    }
}

