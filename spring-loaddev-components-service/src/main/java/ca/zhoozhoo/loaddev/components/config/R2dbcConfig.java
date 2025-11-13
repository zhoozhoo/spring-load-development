package ca.zhoozhoo.loaddev.components.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.r2dbc.convert.R2dbcCustomConversions;
import org.springframework.data.r2dbc.dialect.DialectResolver;
import org.springframework.data.r2dbc.repository.config.EnableR2dbcRepositories;
import org.springframework.r2dbc.connection.R2dbcTransactionManager;
import org.springframework.transaction.ReactiveTransactionManager;

import io.r2dbc.spi.ConnectionFactory;

/**
 * R2DBC configuration for the components service.
 * <p>
 * Configures reactive database connectivity and transaction management
 * for R2DBC repositories. Enables reactive non-blocking database access
 * with transaction support. Registers custom converters for JSR-385 Quantity
 * and JSR-354 MonetaryAmount types to enable JSONB persistence.
 * </p>
 *
 * @author Zhubin Salehi
 */
@Configuration
@EnableR2dbcRepositories
public class R2dbcConfig {

    @Bean
    public ReactiveTransactionManager transactionManager(ConnectionFactory connectionFactory) {
        return new R2dbcTransactionManager(connectionFactory);
    }

    /**
     * Registers custom converters for R2DBC.
     * <p>
     * Includes converters for JSR-385 {@link javax.measure.Quantity} types and
     * JSR-354 {@link javax.money.MonetaryAmount} types to/from PostgreSQL JSONB columns,
     * enabling seamless storage and retrieval of measurement and monetary objects.
     * </p>
     *
     * @param connectionFactory the R2DBC connection factory to determine the dialect
     * @return custom conversions configuration
     */
    @Bean
    public R2dbcCustomConversions r2dbcCustomConversions(ConnectionFactory connectionFactory) {
        return R2dbcCustomConversions.of(DialectResolver.getDialect(connectionFactory), R2dbcConverters.getConverters());
    }
}
