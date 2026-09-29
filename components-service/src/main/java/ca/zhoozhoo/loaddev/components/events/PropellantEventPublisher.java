package ca.zhoozhoo.loaddev.components.events;

import org.springframework.stereotype.Component;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import ca.zhoozhoo.loaddev.common.events.DomainEvent;
import ca.zhoozhoo.loaddev.common.events.outbox.OutboxEventStore;
import ca.zhoozhoo.loaddev.components.model.Propellant;
import reactor.core.publisher.Mono;

/// Publishes domain events for [Propellant] lifecycle changes.
@Component
public class PropellantEventPublisher {

    private static final String TOPIC = "propellants-events";

    private final OutboxEventStore outboxEventStore;

    @SuppressFBWarnings(value = "EI_EXPOSE_REP2", justification = "OutboxEventStore is a Spring-managed collaborator that cannot be defensively copied.")
    public PropellantEventPublisher(OutboxEventStore outboxEventStore) {
        this.outboxEventStore = outboxEventStore;
    }

    public Mono<Void> publishCreated(Propellant propellant) {
        return publish("PropellantCreated", propellant);
    }

    public Mono<Void> publishUpdated(Propellant propellant) {
        return publish("PropellantUpdated", propellant);
    }

    public Mono<Void> publishDeleted(Propellant propellant) {
        return publish("PropellantDeleted", propellant);
    }

    private Mono<Void> publish(String eventType, Propellant propellant) {
        var event = DomainEvent.of(eventType, "Propellant", String.valueOf(propellant.id()), propellant.ownerId(), propellant);
        return outboxEventStore.enqueue(TOPIC, event);
    }
}
