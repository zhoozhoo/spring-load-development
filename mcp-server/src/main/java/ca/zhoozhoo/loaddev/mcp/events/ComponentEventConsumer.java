package ca.zhoozhoo.loaddev.mcp.events;

import java.time.Duration;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import ca.zhoozhoo.loaddev.common.events.DomainEvent;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import lombok.extern.log4j.Log4j2;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.kafka.receiver.KafkaReceiver;
import reactor.kafka.receiver.ReceiverOptions;
import reactor.kafka.receiver.ReceiverRecord;
import tools.jackson.databind.json.JsonMapper;

/// Consumes component domain events and materializes a simple read model in memory.
///
/// This is intentionally minimal: it demonstrates the consumer pattern without
/// introducing a separate database or full CQRS stack. Events are deduplicated by
/// eventId to make retries safe.
@Component
@Log4j2
public class ComponentEventConsumer {

    private final JsonMapper jsonMapper;
    private final Map<String, ComponentReadModel> readModels = new ConcurrentHashMap<>();

    @SuppressFBWarnings(value = "EI_EXPOSE_REP2", justification = "JsonMapper is a Spring-managed collaborator that cannot be defensively copied.")
    public ComponentEventConsumer(JsonMapper jsonMapper) {
        this.jsonMapper = jsonMapper;
    }

    @Bean
    @ConditionalOnProperty(prefix = "loaddev.events.component-read-model", name = "enabled", havingValue = "true")
    public Flux<ReceiverRecord<String, String>> componentEventStream(
            @Value("${loaddev.events.kafka.bootstrap-servers:localhost:9092}") String bootstrapServers) {

        var consumerProps = Map.<String, Object>of(
                ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers,
                ConsumerConfig.GROUP_ID_CONFIG, "mcp-component-read-model",
                ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class,
                ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class,
                ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");

        var receiverOptions = ReceiverOptions.<String, String>create(consumerProps)
                .subscription(Set.of("cases-events", "primers-events", "projectiles-events", "propellants-events"));

        return KafkaReceiver.create(receiverOptions).receive()
                .doOnNext(record -> handle(record.value()))
                .doOnError(e -> log.error("Failed to consume component event", e));
    }

    public Mono<ComponentReadModel> materialize(String eventJson) {
        return Mono.fromCallable(() -> {
            var event = jsonMapper.readValue(eventJson, DomainEvent.class);
            var key = event.eventId();
            var componentType = event.aggregateType();
            var aggregateId = event.aggregateId();
            var location = new ComponentReadModel(
                    componentType,
                    aggregateId,
                    event.ownerId(),
                    componentType,
                    aggregateId,
                    event.occurredAt(),
                    eventJson);
            readModels.put(key, location);
            return location;
        }).doOnNext(model -> log.debug("Materialized component read model for {} {}", model.componentType(), model.aggregateId()));
    }

    private void handle(String eventJson) {
        materialize(eventJson)
                .timeout(Duration.ofSeconds(2))
                .doOnError(e -> log.error("Failed to materialize component read model: {}", e.getMessage(), e))
                .subscribe();
    }
}
