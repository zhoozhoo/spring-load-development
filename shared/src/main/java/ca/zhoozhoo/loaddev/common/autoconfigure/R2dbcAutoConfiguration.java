package ca.zhoozhoo.loaddev.common.autoconfigure;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.data.r2dbc.convert.R2dbcCustomConversions;
import org.springframework.data.r2dbc.dialect.DialectResolver;

import ca.zhoozhoo.loaddev.common.r2dbc.R2dbcConverters;
import io.r2dbc.spi.ConnectionFactory;

/// Auto-configuration for R2DBC with JSR-385 Quantity and JSR-354 MonetaryAmount converters.
///
/// Provides custom [R2dbcCustomConversions] including converters for Quantity,
/// MonetaryAmount, Rifling, and Zeroing JSON serialization to PostgreSQL JSONB.
/// Runs before Spring Boot's data R2DBC auto-configuration so the custom
/// conversions are picked up by the mapping infrastructure.
///
/// @author Zhubin Salehi
@AutoConfiguration(beforeName = "org.springframework.boot.autoconfigure.data.r2dbc.R2dbcDataAutoConfiguration")
@ConditionalOnClass({ConnectionFactory.class, R2dbcCustomConversions.class})
public class R2dbcAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public R2dbcCustomConversions r2dbcCustomConversions(ConnectionFactory connectionFactory) {
        return R2dbcCustomConversions.of(DialectResolver.getDialect(connectionFactory), R2dbcConverters.getConverters());
    }
}
