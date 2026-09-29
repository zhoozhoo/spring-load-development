package ca.zhoozhoo.loaddev.common.events.outbox;

import static org.springframework.data.relational.core.query.Criteria.where;

import org.springframework.data.domain.Sort;
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate;
import org.springframework.data.relational.core.query.Query;
import org.springframework.scheduling.annotation.Scheduled;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import lombok.extern.log4j.Log4j2;
import reactor.core.publisher.Mono;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

import ca.zhoozhoo.loaddev.common.events.DomainEvent;
import ca.zhoozhoo.loaddev.common.events.DomainEventPublisher;

/// Polls the outbox table and relays pending events to Kafka.
///
/// Successfully published rows are marked `PUBLISHED`; failures record the attempt and are
/// retried on the next poll until `maxAttempts` is reached, at which point the row is marked
/// `FAILED` for manual inspection rather than retried forever.
///
/// @author Zhubin Salehi
@Log4j2
public class OutboxRelay {

    private final R2dbcEntityTemplate r2dbcEntityTemplate;

    private final DomainEventPublisher domainEventPublisher;

    private final JsonMapper jsonMapper;

    private final OutboxProperties properties;

    @SuppressFBWarnings(value = "EI_EXPOSE_REP2", justification = "Collaborators are Spring-managed and cannot be defensively copied.")
    public OutboxRelay(R2dbcEntityTemplate r2dbcEntityTemplate, DomainEventPublisher domainEventPublisher,
            JsonMapper jsonMapper, OutboxProperties properties) {
        this.r2dbcEntityTemplate = r2dbcEntityTemplate;
        this.domainEventPublisher = domainEventPublisher;
        this.jsonMapper = jsonMapper;
        this.properties = properties;
    }

    @Scheduled(fixedDelayString = "${loaddev.events.outbox.poll-interval-ms:2000}")
    public void relay() {
        var pendingQuery = Query.query(where("status").is(OutboxEvent.STATUS_PENDING))
                .sort(Sort.by("id"))
                .limit(properties.batchSize());

        r2dbcEntityTemplate.select(pendingQuery, OutboxEvent.class)
                .concatMap(this::publishAndMark)
                .onErrorResume(e -> {
                    log.error("Outbox relay poll failed: {}", e.getMessage(), e);
                    return Mono.empty();
                })
                .subscribe();
    }

    private Mono<OutboxEvent> publishAndMark(OutboxEvent outboxEvent) {
        var payload = jsonMapper.readValue(outboxEvent.payload(), JsonNode.class);
        var event = new DomainEvent<>(outboxEvent.eventId(), outboxEvent.eventType(), outboxEvent.aggregateType(),
                outboxEvent.aggregateId(), outboxEvent.ownerId(), outboxEvent.occurredAt(), payload);

        return domainEventPublisher.publish(outboxEvent.topic(), event)
                .then(Mono.defer(() -> r2dbcEntityTemplate.update(outboxEvent.published())))
                .onErrorResume(e -> {
                    log.warn("Failed to relay outbox event {} (attempt {}): {}", outboxEvent.eventId(),
                            outboxEvent.attempts() + 1, e.getMessage());
                    return r2dbcEntityTemplate.update(outboxEvent.failedAttempt(e.getMessage(), properties.maxAttempts()));
                });
    }
}

