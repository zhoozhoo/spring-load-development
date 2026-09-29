package ca.zhoozhoo.loaddev.components.events;

import org.springframework.stereotype.Component;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import ca.zhoozhoo.loaddev.common.events.DomainEvent;
import ca.zhoozhoo.loaddev.common.events.outbox.OutboxEventStore;
import ca.zhoozhoo.loaddev.components.model.Case;
import reactor.core.publisher.Mono;

/// Publishes domain events for [Case] lifecycle changes.
///
/// Events are enqueued to the outbox for the `cases-events` Kafka topic, keyed by case id, so
/// other services can reliably react to component changes without direct coupling.
@Component
public class CaseEventPublisher {

    private static final String TOPIC = "cases-events";

    private final OutboxEventStore outboxEventStore;

    @SuppressFBWarnings(value = "EI_EXPOSE_REP2", justification = "OutboxEventStore is a Spring-managed collaborator that cannot be defensively copied.")
    public CaseEventPublisher(OutboxEventStore outboxEventStore) {
        this.outboxEventStore = outboxEventStore;
    }

    public Mono<Void> publishCreated(Case caseItem) {
        return publish("CaseCreated", caseItem);
    }

    public Mono<Void> publishUpdated(Case caseItem) {
        return publish("CaseUpdated", caseItem);
    }

    public Mono<Void> publishDeleted(Case caseItem) {
        return publish("CaseDeleted", caseItem);
    }

    private Mono<Void> publish(String eventType, Case caseItem) {
        var event = DomainEvent.of(eventType, "Case", String.valueOf(caseItem.id()), caseItem.ownerId(), caseItem);
        return outboxEventStore.enqueue(TOPIC, event);
    }
}
