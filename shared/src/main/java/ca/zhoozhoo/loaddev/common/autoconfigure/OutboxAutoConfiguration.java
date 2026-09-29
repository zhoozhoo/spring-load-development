package ca.zhoozhoo.loaddev.common.autoconfigure;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate;
import org.springframework.scheduling.annotation.EnableScheduling;

import io.r2dbc.spi.ConnectionFactory;
import reactor.core.publisher.Mono;
import reactor.kafka.sender.KafkaSender;
import tools.jackson.databind.json.JsonMapper;

import ca.zhoozhoo.loaddev.common.events.DomainEventPublisher;
import ca.zhoozhoo.loaddev.common.events.outbox.OutboxEventStore;
import ca.zhoozhoo.loaddev.common.events.outbox.OutboxProperties;
import ca.zhoozhoo.loaddev.common.events.outbox.OutboxRelay;
import ca.zhoozhoo.loaddev.common.events.outbox.R2dbcOutboxEventStore;

/// Auto-configuration for the transactional outbox pattern backing domain event publishing.
///
/// Events are persisted to the `outbox_events` table (via [OutboxEventStore]) in the same
/// database transaction as the triggering aggregate write, then relayed to Kafka on a schedule
/// (via [OutboxRelay]). This guarantees at-least-once delivery even across Kafka outages or
/// service restarts, unlike publishing directly to Kafka from the write path.
///
/// Uses [R2dbcEntityTemplate] rather than a dedicated Spring Data repository so this
/// auto-configuration does not register its own `@EnableR2dbcRepositories`, which would disable
/// Spring Boot's automatic repository scanning for the host service's own repositories.
///
/// Activates only when `reactor-kafka` and R2DBC are on a service's classpath, matching
/// [KafkaEventPublisherAutoConfiguration].
///
/// @author Zhubin Salehi
@AutoConfiguration
@ConditionalOnClass({ KafkaSender.class, ConnectionFactory.class })
@EnableScheduling
@EnableConfigurationProperties(OutboxProperties.class)
public class OutboxAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty(prefix = "loaddev.events.kafka", name = "enabled", havingValue = "true")
    public OutboxEventStore outboxEventStore(R2dbcEntityTemplate r2dbcEntityTemplate, JsonMapper jsonMapper) {
        return new R2dbcOutboxEventStore(r2dbcEntityTemplate, jsonMapper);
    }

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty(prefix = "loaddev.events.kafka", name = "enabled", havingValue = "false", matchIfMissing = true)
    public OutboxEventStore noOpOutboxEventStore() {
        return (topic, event) -> Mono.empty();
    }

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty(prefix = "loaddev.events.kafka", name = "enabled", havingValue = "true")
    public OutboxRelay outboxRelay(R2dbcEntityTemplate r2dbcEntityTemplate, DomainEventPublisher domainEventPublisher,
            JsonMapper jsonMapper, OutboxProperties properties) {
        return new OutboxRelay(r2dbcEntityTemplate, domainEventPublisher, jsonMapper, properties);
    }
}

