package ca.zhoozhoo.loaddev.loads.config;

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

import ca.zhoozhoo.loaddev.loads.model.Unit;
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
            switch (source) {
                case GRAINS:
                    return "gr";
                case INCHES:
                    return "in";
                case MILLIMETERS:
                    return "mm";
                case YARDS:
                    return "yd";
                case METERS:
                    return "m";
                case GRAMS:
                    return "g";
                case FEET:
                    return "ft";
                case FEET_PER_SECOND:
                    return "fps";
                case METERS_PER_SECOND:
                    return "mps";
                default:
                    return source.toString().toLowerCase();
            }
        }
    }

    @ReadingConverter
    static class UnitReadConverter implements Converter<String, Unit> {

        @Override
        public Unit convert(@NonNull String source) {
            switch (source) {
                case "gr":
                    return Unit.GRAINS;
                case "in":
                    return Unit.INCHES;
                case "mm":
                    return Unit.MILLIMETERS;
                case "yd":
                    return Unit.YARDS;
                case "m":
                    return Unit.METERS;
                case "g":
                    return Unit.GRAMS;
                case "ft":
                    return Unit.FEET;
                case "fps":
                    return Unit.FEET_PER_SECOND;
                case "mps":
                    return Unit.METERS_PER_SECOND;
                default:
                    return Unit.valueOf(source.toUpperCase());
            }
        }
    }
}
