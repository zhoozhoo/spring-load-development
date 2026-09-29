package ca.zhoozhoo.loaddev.components.events;

import org.springframework.stereotype.Component;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import ca.zhoozhoo.loaddev.common.events.DomainEvent;
import ca.zhoozhoo.loaddev.common.events.outbox.OutboxEventStore;
import ca.zhoozhoo.loaddev.components.model.Primer;
import reactor.core.publisher.Mono;

/// Publishes domain events for [Primer] lifecycle changes.
@Component
public class PrimerEventPublisher {

    private static final String TOPIC = "primers-events";

    private final OutboxEventStore outboxEventStore;

    @SuppressFBWarnings(value = "EI_EXPOSE_REP2", justification = "OutboxEventStore is a Spring-managed collaborator that cannot be defensively copied.")
    public PrimerEventPublisher(OutboxEventStore outboxEventStore) {
        this.outboxEventStore = outboxEventStore;
    }

    public Mono<Void> publishCreated(Primer primer) {
        return publish("PrimerCreated", primer);
    }

    public Mono<Void> publishUpdated(Primer primer) {
        return publish("PrimerUpdated", primer);
    }

    public Mono<Void> publishDeleted(Primer primer) {
        return publish("PrimerDeleted", primer);
    }

    private Mono<Void> publish(String eventType, Primer primer) {
        var event = DomainEvent.of(eventType, "Primer", String.valueOf(primer.id()), primer.ownerId(), primer);
        return outboxEventStore.enqueue(TOPIC, event);
    }
}
