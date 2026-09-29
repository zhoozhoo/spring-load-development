package ca.zhoozhoo.loaddev.common.events.outbox;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Instant;

import org.junit.jupiter.api.Test;
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate;
import org.springframework.data.relational.core.query.Query;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import tools.jackson.databind.json.JsonMapper;

import ca.zhoozhoo.loaddev.common.events.DomainEvent;
import ca.zhoozhoo.loaddev.common.events.DomainEventPublisher;

class OutboxRelayTest {

    private final OutboxProperties properties = new OutboxProperties(50, 3);

    @Test
    void relay_ShouldPublishPendingEventAndMarkPublished() {
        var r2dbcEntityTemplate = mock(R2dbcEntityTemplate.class);
        var domainEventPublisher = mock(DomainEventPublisher.class);
        var pending = OutboxEvent.pending("cases-events", "Case", "42", "user-1", "CaseCreated", "event-1",
                Instant.now(), "{\"brand\":\"Acme\"}");
        when(r2dbcEntityTemplate.select(any(Query.class), org.mockito.ArgumentMatchers.eq(OutboxEvent.class)))
                .thenReturn(Flux.just(pending));
        when(domainEventPublisher.publish(anyString(), any(DomainEvent.class))).thenReturn(Mono.empty());
        when(r2dbcEntityTemplate.update(any(OutboxEvent.class)))
                .thenAnswer(invocation -> Mono.just(invocation.getArgument(0)));
        var relay = new OutboxRelay(r2dbcEntityTemplate, domainEventPublisher, JsonMapper.builder().build(),
                properties);

        relay.relay();

        var captor = org.mockito.ArgumentCaptor.forClass(OutboxEvent.class);
        verify(r2dbcEntityTemplate).update(captor.capture());
        org.assertj.core.api.Assertions.assertThat(captor.getValue().status()).isEqualTo(OutboxEvent.STATUS_PUBLISHED);
    }

    @Test
    void relay_ShouldRecordFailedAttempt_WhenPublishFails() {
        var r2dbcEntityTemplate = mock(R2dbcEntityTemplate.class);
        var domainEventPublisher = mock(DomainEventPublisher.class);
        var pending = OutboxEvent.pending("cases-events", "Case", "42", "user-1", "CaseCreated", "event-1",
                Instant.now(), "{\"brand\":\"Acme\"}");
        when(r2dbcEntityTemplate.select(any(Query.class), org.mockito.ArgumentMatchers.eq(OutboxEvent.class)))
                .thenReturn(Flux.just(pending));
        when(domainEventPublisher.publish(anyString(), any(DomainEvent.class)))
                .thenReturn(Mono.error(new RuntimeException("broker unavailable")));
        when(r2dbcEntityTemplate.update(any(OutboxEvent.class)))
                .thenAnswer(invocation -> Mono.just(invocation.getArgument(0)));
        var relay = new OutboxRelay(r2dbcEntityTemplate, domainEventPublisher, JsonMapper.builder().build(),
                properties);

        relay.relay();

        var captor = org.mockito.ArgumentCaptor.forClass(OutboxEvent.class);
        verify(r2dbcEntityTemplate).update(captor.capture());
        var saved = captor.getValue();
        org.assertj.core.api.Assertions.assertThat(saved.status()).isEqualTo(OutboxEvent.STATUS_PENDING);
        org.assertj.core.api.Assertions.assertThat(saved.attempts()).isEqualTo(1);
    }
}

