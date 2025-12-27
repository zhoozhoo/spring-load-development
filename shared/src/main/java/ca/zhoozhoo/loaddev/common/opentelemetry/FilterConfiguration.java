package ca.zhoozhoo.loaddev.common.opentelemetry;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.micrometer.tracing.Tracer;

@Configuration(proxyBeanMethods = false)
public class FilterConfiguration {

    @Bean
    HeaderLoggerFilter headerLoggerFilter() {
        return new HeaderLoggerFilter();
    }

    @Bean
    AddTraceIdFilter addTraceIdFilter(Tracer tracer) {
        return new AddTraceIdFilter(tracer);
    }

}