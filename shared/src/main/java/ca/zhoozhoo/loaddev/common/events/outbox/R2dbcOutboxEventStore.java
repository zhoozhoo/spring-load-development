package ca.zhoozhoo.loaddev.common.events.outbox;

import org.springframework.data.r2dbc.core.R2dbcEntityTemplate;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import reactor.core.publisher.Mono;
import tools.jackson.databind.json.JsonMapper;

import ca.zhoozhoo.loaddev.common.events.DomainEvent;

/// [R2dbcEntityTemplate]-backed [OutboxEventStore].
///
/// Uses the template API directly rather than a Spring Data repository, so this class does not
/// require its own `@EnableR2dbcRepositories` registration and cannot interfere with a host
/// service's own repository scanning.
///
/// @author Zhubin Salehi
public class R2dbcOutboxEventStore implements OutboxEventStore {

    private final R2dbcEntityTemplate r2dbcEntityTemplate;

    private final JsonMapper jsonMapper;

    @SuppressFBWarnings(value = "EI_EXPOSE_REP2", justification = "R2dbcEntityTemplate and JsonMapper are Spring-managed collaborators that cannot be defensively copied.")
    public R2dbcOutboxEventStore(R2dbcEntityTemplate r2dbcEntityTemplate, JsonMapper jsonMapper) {
        this.r2dbcEntityTemplate = r2dbcEntityTemplate;
        this.jsonMapper = jsonMapper;
    }

    @Override
    public Mono<Void> enqueue(String topic, DomainEvent<?> event) {
        var payload = jsonMapper.writeValueAsString(jsonMapper.valueToTree(event.payload()));
        var outboxEvent = OutboxEvent.pending(topic, event.aggregateType(), event.aggregateId(), event.ownerId(),
                event.eventType(), event.eventId(), event.occurredAt(), payload);

        return r2dbcEntityTemplate.insert(outboxEvent).then();
    }
}
