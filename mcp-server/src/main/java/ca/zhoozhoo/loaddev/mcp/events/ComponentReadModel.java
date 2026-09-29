package ca.zhoozhoo.loaddev.mcp.events;

import java.time.Instant;

/// Materialized, query-friendly snapshot derived from component domain events.
/// This is intentionally lightweight and keeps the read side independent from the
/// write-side component records.
public record ComponentReadModel(
        String aggregateType,
        String aggregateId,
        String ownerId,
        String componentType,
        String componentId,
        Instant lastUpdated,
        String payloadJson) {
}
