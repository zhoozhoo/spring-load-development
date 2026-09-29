package ca.zhoozhoo.loaddev.common.events;

import java.time.Instant;
import java.util.UUID;

/// Generic envelope for domain events published by services across the platform.
///
/// @param eventId       unique identifier for this event instance
/// @param eventType     the event name, e.g. `LoadCreated`
/// @param aggregateType the domain type the event relates to, e.g. `Load`
/// @param aggregateId   the identifier of the aggregate the event relates to
/// @param ownerId       the user that owns the aggregate, for data isolation downstream
/// @param occurredAt    when the event occurred
/// @param payload       the event payload
///
/// @author Zhubin Salehi
public record DomainEvent<T>(
        String eventId,
        String eventType,
        String aggregateType,
        String aggregateId,
        String ownerId,
        Instant occurredAt,
        T payload) {

    /// Creates a new event with a generated id and the current timestamp.
    public static <T> DomainEvent<T> of(String eventType, String aggregateType, String aggregateId, String ownerId,
            T payload) {
        return new DomainEvent<>(UUID.randomUUID().toString(), eventType, aggregateType, aggregateId, ownerId,
                Instant.now(), payload);
    }
}
