package ca.zhoozhoo.loaddev.loads.events;

import org.springframework.stereotype.Component;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import ca.zhoozhoo.loaddev.common.events.DomainEvent;
import ca.zhoozhoo.loaddev.common.events.outbox.OutboxEventStore;
import ca.zhoozhoo.loaddev.loads.model.Load;
import reactor.core.publisher.Mono;

/// Publishes domain events for [Load] lifecycle changes.
///
/// Events are enqueued to the outbox for the `loads-events` Kafka topic, keyed by load id, so
/// other services (search indexing, analytics, MCP tooling) can reliably react to load changes
/// without being directly coupled to the Loads Service.
///
/// @author Zhubin Salehi
@Component
public class LoadEventPublisher {

    private static final String TOPIC = "loads-events";

    private final OutboxEventStore outboxEventStore;

    @SuppressFBWarnings(value = "EI_EXPOSE_REP2", justification = "OutboxEventStore is a Spring-managed collaborator that cannot be defensively copied.")
    public LoadEventPublisher(OutboxEventStore outboxEventStore) {
        this.outboxEventStore = outboxEventStore;
    }

    /// Publishes a `LoadCreated` event.
    public Mono<Void> publishCreated(Load load) {
        return publish("LoadCreated", load);
    }

    /// Publishes a `LoadUpdated` event.
    public Mono<Void> publishUpdated(Load load) {
        return publish("LoadUpdated", load);
    }

    /// Publishes a `LoadDeleted` event.
    public Mono<Void> publishDeleted(Load load) {
        return publish("LoadDeleted", load);
    }

    private Mono<Void> publish(String eventType, Load load) {
        var event = DomainEvent.of(eventType, "Load", String.valueOf(load.id()), load.ownerId(), load);

        return outboxEventStore.enqueue(TOPIC, event);
    }
}
