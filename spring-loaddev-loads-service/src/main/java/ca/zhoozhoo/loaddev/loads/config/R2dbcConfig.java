package ca.zhoozhoo.loaddev.loads.config;

import java.util.ArrayList;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.r2dbc.convert.R2dbcCustomConversions;
import org.springframework.data.r2dbc.dialect.DialectResolver;
import org.springframework.data.r2dbc.repository.config.EnableR2dbcRepositories;
import org.springframework.r2dbc.connection.R2dbcTransactionManager;
import org.springframework.transaction.ReactiveTransactionManager;

import io.r2dbc.spi.ConnectionFactory;

/**
 * Configuration class for R2DBC (Reactive Relational Database Connectivity).
 * <p>
 * This configuration enables R2DBC repositories and provides a reactive transaction manager
 * for managing database transactions in a non-blocking, reactive manner with PostgreSQL.
 * It also registers custom converters for handling {@link javax.measure.Quantity} types.
 * </p>
 *
 * @author Zhubin Salehi
 */
@Configuration
@EnableR2dbcRepositories
public class R2dbcConfig {

    /**
     * Configures a reactive transaction manager for R2DBC.
     *
     * @param connectionFactory the R2DBC connection factory
     * @return a reactive transaction manager
     */
    @Bean
    public ReactiveTransactionManager transactionManager(ConnectionFactory connectionFactory) {
        return new R2dbcTransactionManager(connectionFactory);
    }

    /**
     * Registers custom converters for R2DBC.
     * <p>
     * This includes converters for {@link javax.measure.Quantity} types to/from
     * PostgreSQL JSONB columns, enabling seamless storage and retrieval of
     * measurement objects with their units.
     * </p>
     *
     * @param connectionFactory the R2DBC connection factory to determine the dialect
     * @return custom conversions configuration
     */
    @Bean
    public R2dbcCustomConversions r2dbcCustomConversions(ConnectionFactory connectionFactory) {
        var converters = new ArrayList<>();
        converters.addAll(R2dbcConverters.getConverters());
        
        return R2dbcCustomConversions.of(DialectResolver.getDialect(connectionFactory), converters);
    }
}
