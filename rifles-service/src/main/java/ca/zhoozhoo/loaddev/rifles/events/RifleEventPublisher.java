package ca.zhoozhoo.loaddev.rifles.events;

import org.springframework.stereotype.Component;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import ca.zhoozhoo.loaddev.common.events.DomainEvent;
import ca.zhoozhoo.loaddev.common.events.outbox.OutboxEventStore;
import ca.zhoozhoo.loaddev.rifles.model.Rifle;
import reactor.core.publisher.Mono;

/// Publishes domain events for [Rifle] lifecycle changes.
///
/// Events are enqueued to the outbox for the `rifles-events` Kafka topic, keyed by rifle id,
/// so other services can reliably react to rifle changes without direct coupling.
@Component
public class RifleEventPublisher {

    private static final String TOPIC = "rifles-events";

    private final OutboxEventStore outboxEventStore;

    @SuppressFBWarnings(value = "EI_EXPOSE_REP2", justification = "OutboxEventStore is a Spring-managed collaborator that cannot be defensively copied.")
    public RifleEventPublisher(OutboxEventStore outboxEventStore) {
        this.outboxEventStore = outboxEventStore;
    }

    public Mono<Void> publishCreated(Rifle rifle) {
        return publish("RifleCreated", rifle);
    }

    public Mono<Void> publishUpdated(Rifle rifle) {
        return publish("RifleUpdated", rifle);
    }

    public Mono<Void> publishDeleted(Rifle rifle) {
        return publish("RifleDeleted", rifle);
    }

    private Mono<Void> publish(String eventType, Rifle rifle) {
        var event = DomainEvent.of(eventType, "Rifle", String.valueOf(rifle.id()), rifle.ownerId(), rifle);
        return outboxEventStore.enqueue(TOPIC, event);
    }
}
