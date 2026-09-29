package ca.zhoozhoo.loaddev.common.events.outbox;

import java.time.Instant;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

/// Persisted record of a domain event awaiting reliable delivery to Kafka.
///
/// Written in the same database transaction as the triggering aggregate write (see
/// [OutboxEventStore]), then relayed to Kafka asynchronously by [OutboxRelay], so an event is
/// never lost even if the broker is unavailable at write time.
///
/// @author Zhubin Salehi
@Table("outbox_events")
public record OutboxEvent(

        @Id Long id,

        @Column("event_id") String eventId,

        @Column("topic") String topic,

        @Column("aggregate_type") String aggregateType,

        @Column("aggregate_id") String aggregateId,

        @Column("owner_id") String ownerId,

        @Column("event_type") String eventType,

        @Column("payload") String payload,

        @Column("occurred_at") Instant occurredAt,

        @Column("status") String status,

        @Column("attempts") int attempts,

        @Column("last_error") String lastError,

        @Column("created_at") Instant createdAt,

        @Column("published_at") Instant publishedAt) {

    public static final String STATUS_PENDING = "PENDING";

    public static final String STATUS_PUBLISHED = "PUBLISHED";

    public static final String STATUS_FAILED = "FAILED";

    /// Creates a new pending outbox row for a domain event.
    public static OutboxEvent pending(String topic, String aggregateType, String aggregateId, String ownerId,
            String eventType, String eventId, Instant occurredAt, String payload) {
        return new OutboxEvent(null, eventId, topic, aggregateType, aggregateId, ownerId, eventType, payload,
                occurredAt, STATUS_PENDING, 0, null, Instant.now(), null);
    }

    /// Returns a copy marked as successfully published.
    public OutboxEvent published() {
        return new OutboxEvent(id, eventId, topic, aggregateType, aggregateId, ownerId, eventType, payload,
                occurredAt, STATUS_PUBLISHED, attempts, null, createdAt, Instant.now());
    }

    /// Returns a copy recording a failed delivery attempt, moving to `FAILED` once `maxAttempts` is reached.
    public OutboxEvent failedAttempt(String error, int maxAttempts) {
        var nextAttempts = attempts + 1;
        var nextStatus = nextAttempts >= maxAttempts ? STATUS_FAILED : STATUS_PENDING;
        return new OutboxEvent(id, eventId, topic, aggregateType, aggregateId, ownerId, eventType, payload,
                occurredAt, nextStatus, nextAttempts, error, createdAt, publishedAt);
    }
}
