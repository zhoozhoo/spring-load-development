package ca.zhoozhoo.loaddev.rifles.config;

import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.r2dbc.config.AbstractR2dbcConfiguration;
import org.springframework.data.r2dbc.repository.config.EnableR2dbcRepositories;
import org.springframework.r2dbc.connection.R2dbcTransactionManager;
import org.springframework.transaction.ReactiveTransactionManager;

import io.r2dbc.spi.ConnectionFactory;

/**
 * Configuration class for R2DBC (Reactive Relational Database Connectivity) with JSR-385 support.
 * <p>
 * This configuration enables reactive database access for the rifles service,
 * providing non-blocking database operations with PostgreSQL. It configures
 * the reactive transaction manager for handling database transactions in a
 * reactive programming model and registers custom converters for JSR-385 Quantity types.
 * </p>
 *
 * @author Zhubin Salehi
 */
@Configuration
@EnableR2dbcRepositories
public class R2dbcConfig extends AbstractR2dbcConfiguration {

    private final ConnectionFactory connectionFactory;

    public R2dbcConfig(ConnectionFactory connectionFactory) {
        this.connectionFactory = connectionFactory;
    }

    @Override
    public ConnectionFactory connectionFactory() {
        return connectionFactory;
    }

    @Override
    protected List<Object> getCustomConverters() {
        return R2dbcConverters.getConverters();
    }

    @Bean
    public ReactiveTransactionManager transactionManager(ConnectionFactory connectionFactory) {
        return new R2dbcTransactionManager(connectionFactory);
    }
}
