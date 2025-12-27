package ca.zhoozhoo.loaddev.common.opentelemetry;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilterChain;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

@ExtendWith(MockitoExtension.class)
class HeaderLoggerFilterTest {

    @Mock
    private WebFilterChain filterChain;

    private HeaderLoggerFilter filter;
    private ListAppender<ILoggingEvent> listAppender;
    private Logger logger;

    @BeforeEach
    void setUp() {
        filter = new HeaderLoggerFilter();

        // Set up logback test appender
        logger = (Logger) org.slf4j.LoggerFactory.getLogger(HeaderLoggerFilter.class);
        listAppender = new ListAppender<>();
        listAppender.start();
        logger.addAppender(listAppender);
        logger.setLevel(Level.DEBUG);
    }

    @Test
    void shouldLogHeadersWithSingleValue() {
        // Given
        ServerWebExchange exchange = MockServerWebExchange.from(
                MockServerHttpRequest.get("/test")
                        .header("User-Agent", "Test-Agent")
                        .header("Accept", "application/json")
                        .build());

        when(filterChain.filter(exchange)).thenReturn(Mono.empty());

        // When
        Mono<Void> result = filter.filter(exchange, filterChain);

        // Then
        StepVerifier.create(result)
                .verifyComplete();

        verify(filterChain).filter(exchange);

        // Verify headers were logged at DEBUG level
        long headerLogCount = listAppender.list.stream()
                .filter(event -> event.getLevel() == Level.DEBUG)
                .filter(event -> event.getMessage().contains(":"))
                .count();

        // Should have logged at least the headers we added
        assert headerLogCount >= 2;
    }

    @Test
    void shouldLogHeadersWithMultipleValues() {
        // Given
        ServerWebExchange exchange = MockServerWebExchange.from(
                MockServerHttpRequest.get("/test")
                        .header("Accept", "application/json", "text/html")
                        .header("Accept-Language", "en-US", "en", "fr")
                        .build());

        when(filterChain.filter(exchange)).thenReturn(Mono.empty());

        // When
        Mono<Void> result = filter.filter(exchange, filterChain);

        // Then
        StepVerifier.create(result)
                .verifyComplete();

        verify(filterChain).filter(exchange);

        // Verify multiple-value headers were logged
        long headerLogCount = listAppender.list.stream()
                .filter(event -> event.getLevel() == Level.DEBUG)
                .filter(event -> event.getMessage().contains(":"))
                .count();

        assert headerLogCount >= 2;
    }

    @Test
    void shouldContinueFilterChainWhenNoHeaders() {
        // Given
        ServerWebExchange exchange = MockServerWebExchange.from(
                MockServerHttpRequest.get("/test").build());

        when(filterChain.filter(exchange)).thenReturn(Mono.empty());

        // When
        Mono<Void> result = filter.filter(exchange, filterChain);

        // Then
        StepVerifier.create(result)
                .verifyComplete();

        verify(filterChain).filter(exchange);
    }

    @Test
    void shouldContinueFilterChainOnError() {
        // Given
        ServerWebExchange exchange = MockServerWebExchange.from(
                MockServerHttpRequest.get("/test")
                        .header("Content-Type", "application/json")
                        .build());

        RuntimeException expectedException = new RuntimeException("Chain error");
        when(filterChain.filter(exchange)).thenReturn(Mono.error(expectedException));

        // When
        Mono<Void> result = filter.filter(exchange, filterChain);

        // Then
        StepVerifier.create(result)
                .expectError(RuntimeException.class)
                .verify();

        verify(filterChain).filter(exchange);
    }

    @Test
    void shouldLogStandardHttpHeaders() {
        // Given
        ServerWebExchange exchange = MockServerWebExchange.from(
                MockServerHttpRequest.post("/api/resource")
                        .header("Content-Type", "application/json")
                        .header("Authorization", "Bearer token123")
                        .header("X-Request-ID", "req-456")
                        .build());

        when(filterChain.filter(exchange)).thenReturn(Mono.empty());

        // When
        Mono<Void> result = filter.filter(exchange, filterChain);

        // Then
        StepVerifier.create(result)
                .verifyComplete();

        // Verify headers were logged
        boolean hasContentTypeLog = listAppender.list.stream()
                .anyMatch(event -> event.getFormattedMessage().contains("Content-Type"));

        boolean hasAuthLog = listAppender.list.stream()
                .anyMatch(event -> event.getFormattedMessage().contains("Authorization"));

        assert hasContentTypeLog;
        assert hasAuthLog;
    }
}
