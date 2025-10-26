package ca.zhoozhoo.loaddev.rifles.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.r2dbc.repository.config.EnableR2dbcRepositories;
import org.springframework.r2dbc.connection.R2dbcTransactionManager;
import org.springframework.transaction.ReactiveTransactionManager;

import io.r2dbc.spi.ConnectionFactory;

/**
 * Configuration class for R2DBC (Reactive Relational Database Connectivity).
 * <p>
 * This configuration enables reactive database access for the rifles service,
 * providing non-blocking database operations with PostgreSQL. It configures
 * the reactive transaction manager for handling database transactions in a
 * reactive programming model.
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
}
