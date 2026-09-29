package ca.zhoozhoo.loaddev.common.events;

import org.apache.kafka.clients.producer.ProducerRecord;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import reactor.core.publisher.Mono;
import reactor.kafka.sender.KafkaSender;
import reactor.kafka.sender.SenderRecord;
import tools.jackson.databind.json.JsonMapper;

/// Kafka-backed implementation of [DomainEventPublisher].
///
/// Serializes events to JSON and sends them with a reactive [KafkaSender] so
/// publishing never blocks the calling reactive pipeline. The event's
/// aggregate id is used as the Kafka record key to preserve per-aggregate
/// ordering.
///
/// @author Zhubin Salehi
public class KafkaDomainEventPublisher implements DomainEventPublisher {

    private final KafkaSender<String, String> kafkaSender;
    private final JsonMapper jsonMapper;

    @SuppressFBWarnings(value = "EI_EXPOSE_REP2", justification = "KafkaSender and JsonMapper are Spring-managed collaborators that cannot be defensively copied.")
    public KafkaDomainEventPublisher(KafkaSender<String, String> kafkaSender, JsonMapper jsonMapper) {
        this.kafkaSender = kafkaSender;
        this.jsonMapper = jsonMapper;
    }

    @Override
    public Mono<Void> publish(String topic, DomainEvent<?> event) {
        var producerRecord = new ProducerRecord<>(topic, event.aggregateId(), jsonMapper.writeValueAsString(event));

        return kafkaSender.send(Mono.just(SenderRecord.create(producerRecord, event.eventId())))
                .then();
    }
}
