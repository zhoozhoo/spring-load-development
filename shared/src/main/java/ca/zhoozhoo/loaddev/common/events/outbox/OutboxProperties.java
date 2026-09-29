package ca.zhoozhoo.loaddev.common.events.outbox;

import org.springframework.boot.context.properties.ConfigurationProperties;

/// Configuration properties for the transactional outbox relay.
///
/// @param batchSize   maximum number of pending events relayed per poll
/// @param maxAttempts number of failed delivery attempts before an event is marked `FAILED`
///
/// @author Zhubin Salehi
@ConfigurationProperties(prefix = "loaddev.events.outbox")
public record OutboxProperties(int batchSize, int maxAttempts) {

    public OutboxProperties {
        if (batchSize <= 0) {
            batchSize = 50;
        }
        if (maxAttempts <= 0) {
            maxAttempts = 10;
        }
    }
}
