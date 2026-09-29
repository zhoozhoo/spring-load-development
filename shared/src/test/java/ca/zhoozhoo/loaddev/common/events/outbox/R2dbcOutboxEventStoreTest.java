package ca.zhoozhoo.loaddev.common.events.outbox;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate;

import reactor.core.publisher.Mono;
import tools.jackson.databind.json.JsonMapper;

import ca.zhoozhoo.loaddev.common.events.DomainEvent;

class R2dbcOutboxEventStoreTest {

    @Test
    void enqueue_ShouldInsertPendingOutboxRowWithSerializedPayload() {
        var r2dbcEntityTemplate = mock(R2dbcEntityTemplate.class);
        when(r2dbcEntityTemplate.insert(any(OutboxEvent.class)))
                .thenAnswer(invocation -> Mono.just(invocation.getArgument(0)));
        var store = new R2dbcOutboxEventStore(r2dbcEntityTemplate, JsonMapper.builder().build());
        var event = DomainEvent.of("CaseCreated", "Case", "42", "user-1", new EventPayload("Acme"));

        store.enqueue("cases-events", event).block();

        var captor = org.mockito.ArgumentCaptor.forClass(OutboxEvent.class);
        verify(r2dbcEntityTemplate).insert(captor.capture());
        var saved = captor.getValue();
        assertThat(saved.status()).isEqualTo(OutboxEvent.STATUS_PENDING);
        assertThat(saved.topic()).isEqualTo("cases-events");
        assertThat(saved.eventId()).isEqualTo(event.eventId());
        assertThat(saved.payload()).contains("Acme");
    }

    private record EventPayload(String brand) {
    }
}

