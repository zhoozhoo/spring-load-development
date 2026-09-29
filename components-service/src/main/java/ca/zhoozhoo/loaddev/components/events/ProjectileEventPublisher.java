package ca.zhoozhoo.loaddev.components.events;

import org.springframework.stereotype.Component;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import ca.zhoozhoo.loaddev.common.events.DomainEvent;
import ca.zhoozhoo.loaddev.common.events.outbox.OutboxEventStore;
import ca.zhoozhoo.loaddev.components.model.Projectile;
import reactor.core.publisher.Mono;

/// Publishes domain events for [Projectile] lifecycle changes.
@Component
public class ProjectileEventPublisher {

    private static final String TOPIC = "projectiles-events";

    private final OutboxEventStore outboxEventStore;

    @SuppressFBWarnings(value = "EI_EXPOSE_REP2", justification = "OutboxEventStore is a Spring-managed collaborator that cannot be defensively copied.")
    public ProjectileEventPublisher(OutboxEventStore outboxEventStore) {
        this.outboxEventStore = outboxEventStore;
    }

    public Mono<Void> publishCreated(Projectile projectile) {
        return publish("ProjectileCreated", projectile);
    }

    public Mono<Void> publishUpdated(Projectile projectile) {
        return publish("ProjectileUpdated", projectile);
    }

    public Mono<Void> publishDeleted(Projectile projectile) {
        return publish("ProjectileDeleted", projectile);
    }

    private Mono<Void> publish(String eventType, Projectile projectile) {
        var event = DomainEvent.of(eventType, "Projectile", String.valueOf(projectile.id()), projectile.ownerId(), projectile);
        return outboxEventStore.enqueue(TOPIC, event);
    }
}
