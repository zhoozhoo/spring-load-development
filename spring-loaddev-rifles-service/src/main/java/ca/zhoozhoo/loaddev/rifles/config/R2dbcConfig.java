package ca.zhoozhoo.loaddev.rifles.config;

import java.util.ArrayList;
import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.convert.ReadingConverter;
import org.springframework.data.convert.WritingConverter;
import org.springframework.data.r2dbc.convert.R2dbcCustomConversions;
import org.springframework.data.r2dbc.dialect.PostgresDialect;
import org.springframework.data.r2dbc.repository.config.EnableR2dbcRepositories;
import org.springframework.lang.NonNull;
import org.springframework.r2dbc.connection.R2dbcTransactionManager;
import org.springframework.transaction.ReactiveTransactionManager;

import ca.zhoozhoo.loaddev.rifles.model.Unit;
import io.r2dbc.spi.ConnectionFactory;

@Configuration
@EnableR2dbcRepositories
public class R2dbcConfig {

    @Bean
    public ReactiveTransactionManager transactionManager(ConnectionFactory connectionFactory) {
        return new R2dbcTransactionManager(connectionFactory);
    }

    @Bean
    public R2dbcCustomConversions customConversions() {
        List<Converter<?, ?>> converters = new ArrayList<>();
        converters.add(new UnitWriteConverter());
        converters.add(new UnitReadConverter());

        return R2dbcCustomConversions.of(PostgresDialect.INSTANCE, converters);
    }

    @WritingConverter
    static class UnitWriteConverter implements Converter<Unit, String> {

        @Override
        public String convert(@NonNull Unit source) {
            return source.getValue();
        }
    }

    @ReadingConverter
    static class UnitReadConverter implements Converter<String, Unit> {

        @Override
        public Unit convert(@NonNull String source) {
            for (Unit unit : Unit.values()) {
                if (unit.getValue().equals(source)) {
                    return unit;
                }
            }

            throw new IllegalArgumentException("Unknown unit value: " + source);
        }
    }
}
