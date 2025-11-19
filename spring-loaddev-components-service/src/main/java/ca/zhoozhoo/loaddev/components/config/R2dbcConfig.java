package ca.zhoozhoo.loaddev.components.config;

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
 * R2DBC configuration for reactive PostgreSQL with Quantity converters.
 *
 * @author Zhubin Salehi
 */
@Configuration
@EnableR2dbcRepositories
public class R2dbcConfig {

    /**
     * Configures reactive transaction manager.
     *
     * @param connectionFactory R2DBC connection factory
     * @return reactive transaction manager
     */
    @Bean
    public ReactiveTransactionManager transactionManager(ConnectionFactory connectionFactory) {
        return new R2dbcTransactionManager(connectionFactory);
    }

    /**
     * Registers custom Quantity converters for PostgreSQL JSONB.
     *
     * @param connectionFactory R2DBC connection factory
     * @return custom conversions configuration
     */
    @Bean
    public R2dbcCustomConversions r2dbcCustomConversions(ConnectionFactory connectionFactory) {
        var converters = new ArrayList<>();
        converters.addAll(R2dbcConverters.getConverters());
        
        return R2dbcCustomConversions.of(DialectResolver.getDialect(connectionFactory), converters);
    }
}
