package ca.zhoozhoo.loaddev.common.events.outbox;

import reactor.core.publisher.Mono;

import ca.zhoozhoo.loaddev.common.events.DomainEvent;

/// Durably records domain events for reliable, eventual delivery to their transport (Kafka
/// today) via [OutboxRelay], decoupling the write path from broker availability.
///
/// @author Zhubin Salehi
public interface OutboxEventStore {

    /// Enqueues a domain event for later delivery to the given topic.
    ///
    /// @param topic the destination topic
    /// @param event the event to enqueue
    /// @return a Mono<Void> that completes when the event has been durably recorded
    Mono<Void> enqueue(String topic, DomainEvent<?> event);
}
