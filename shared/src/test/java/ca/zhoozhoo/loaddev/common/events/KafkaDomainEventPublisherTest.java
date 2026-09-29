package ca.zhoozhoo.loaddev.common.events;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.reactivestreams.Publisher;

import reactor.core.publisher.Flux;
import reactor.kafka.sender.KafkaSender;
import tools.jackson.databind.json.JsonMapper;

class KafkaDomainEventPublisherTest {

    @Test
    @SuppressWarnings("unchecked")
    void publish_ShouldSerializeAndSendEvent() {
        KafkaSender<String, String> kafkaSender = org.mockito.Mockito.mock(KafkaSender.class);
        when(kafkaSender.send(any(Publisher.class))).thenReturn(Flux.empty());
        var publisher = new KafkaDomainEventPublisher(kafkaSender, JsonMapper.builder().build());
        var event = DomainEvent.of("CaseCreated", "Case", "42", "user-1", new EventPayload("Acme"));

        publisher.publish("cases-events", event).block();

        verify(kafkaSender).send(any(Publisher.class));
    }

    private record EventPayload(String brand) {
    }
}
