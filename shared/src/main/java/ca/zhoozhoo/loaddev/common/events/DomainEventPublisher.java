package ca.zhoozhoo.loaddev.common.events;

import reactor.core.publisher.Mono;

/// Reactive, non-blocking abstraction for publishing domain events.
///
/// Services depend on this interface rather than a specific broker so the
/// underlying transport (Kafka today) can evolve without changing callers.
///
/// @author Zhubin Salehi
public interface DomainEventPublisher {

    /// Publishes a domain event to the given topic.
    ///
    /// @param topic the destination topic
    /// @param event the event to publish
    /// @return a Mono<Void> that completes when the event has been sent
    Mono<Void> publish(String topic, DomainEvent<?> event);
}
