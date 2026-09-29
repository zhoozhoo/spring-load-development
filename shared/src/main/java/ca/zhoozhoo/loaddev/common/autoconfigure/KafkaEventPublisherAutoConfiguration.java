package ca.zhoozhoo.loaddev.common.autoconfigure;

import java.util.Map;

import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

import ca.zhoozhoo.loaddev.common.events.DomainEventPublisher;
import ca.zhoozhoo.loaddev.common.events.KafkaDomainEventPublisher;
import ca.zhoozhoo.loaddev.common.events.KafkaEventProperties;
import reactor.core.publisher.Mono;
import reactor.kafka.sender.KafkaSender;
import reactor.kafka.sender.SenderOptions;
import tools.jackson.databind.json.JsonMapper;

/// Auto-configuration for reactive, Kafka-backed domain event publishing.
///
/// Activates only when `reactor-kafka` is on the classpath, so services opt in
/// simply by adding the dependency. Provides a [DomainEventPublisher] bean
/// that services can inject to publish domain events (e.g. load
/// created/updated/deleted) without blocking their reactive pipelines.
///
/// @author Zhubin Salehi
@AutoConfiguration
@ConditionalOnClass(KafkaSender.class)
@EnableConfigurationProperties(KafkaEventProperties.class)
public class KafkaEventPublisherAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty(prefix = "loaddev.events.kafka", name = "enabled", havingValue = "true")
    public KafkaSender<String, String> domainEventKafkaSender(KafkaEventProperties properties) {
        var producerProps = Map.<String, Object>of(
                ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, properties.bootstrapServers(),
                ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class,
                ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class,
                ProducerConfig.ACKS_CONFIG, "all");

        return KafkaSender.create(SenderOptions.create(producerProps));
    }

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty(prefix = "loaddev.events.kafka", name = "enabled", havingValue = "true")
    public DomainEventPublisher domainEventPublisher(KafkaSender<String, String> domainEventKafkaSender,
            JsonMapper jsonMapper) {
        return new KafkaDomainEventPublisher(domainEventKafkaSender, jsonMapper);
    }

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty(prefix = "loaddev.events.kafka", name = "enabled", havingValue = "false", matchIfMissing = true)
    public DomainEventPublisher noOpDomainEventPublisher() {
        return (topic, event) -> Mono.empty();
    }
}
