package ca.zhoozhoo.loaddev.common.events;

import org.springframework.boot.context.properties.ConfigurationProperties;

/// Configuration properties for the Kafka-backed domain event publisher.
///
/// @param bootstrapServers Kafka bootstrap servers, e.g. `localhost:9092`
///
/// @author Zhubin Salehi
@ConfigurationProperties(prefix = "loaddev.events.kafka")
public record KafkaEventProperties(String bootstrapServers) {

    public KafkaEventProperties {
        if (bootstrapServers == null || bootstrapServers.isBlank()) {
            bootstrapServers = "localhost:9092";
        }
    }
}
