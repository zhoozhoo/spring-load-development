package ca.zhoozhoo.loaddev.common.opentelemetry;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilterChain;

import io.micrometer.tracing.CurrentTraceContext;
import io.micrometer.tracing.TraceContext;
import io.micrometer.tracing.Tracer;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

@ExtendWith(MockitoExtension.class)
class AddTraceIdFilterTest {

    @Mock
    private Tracer tracer;

    @Mock
    private CurrentTraceContext currentTraceContext;

    @Mock
    private TraceContext traceContext;

    @Mock
    private WebFilterChain filterChain;

    private AddTraceIdFilter filter;

    @BeforeEach
    void setUp() {
        filter = new AddTraceIdFilter(tracer);
        when(tracer.currentTraceContext()).thenReturn(currentTraceContext);
    }

    @Test
    void shouldAddTraceIdHeaderWhenTraceContextExists() {
        // Given
        String expectedTraceId = "1234567890abcdef";
        when(currentTraceContext.context()).thenReturn(traceContext);
        when(traceContext.traceId()).thenReturn(expectedTraceId);

        ServerWebExchange exchange = MockServerWebExchange.from(
                MockServerHttpRequest.get("/test").build());

        when(filterChain.filter(exchange)).thenReturn(Mono.empty());

        // When
        Mono<Void> result = filter.filter(exchange, filterChain);

        // Then
        StepVerifier.create(result)
                .verifyComplete();

        HttpHeaders responseHeaders = exchange.getResponse().getHeaders();
        assertThat(responseHeaders.getFirst("X-Trace-Id")).isEqualTo(expectedTraceId);
        verify(filterChain).filter(exchange);
    }

    @Test
    void shouldNotAddTraceIdHeaderWhenTraceContextIsNull() {
        // Given
        when(currentTraceContext.context()).thenReturn(null);

        ServerWebExchange exchange = MockServerWebExchange.from(
                MockServerHttpRequest.get("/test").build());

        when(filterChain.filter(exchange)).thenReturn(Mono.empty());

        // When
        Mono<Void> result = filter.filter(exchange, filterChain);

        // Then
        StepVerifier.create(result)
                .verifyComplete();

        HttpHeaders responseHeaders = exchange.getResponse().getHeaders();
        assertThat(responseHeaders.get("X-Trace-Id")).isNull();
        verify(filterChain).filter(exchange);
    }

    @Test
    void shouldNotAddTraceIdHeaderWhenTraceIdIsNull() {
        // Given
        when(currentTraceContext.context()).thenReturn(traceContext);
        when(traceContext.traceId()).thenReturn(null);

        ServerWebExchange exchange = MockServerWebExchange.from(
                MockServerHttpRequest.get("/test").build());

        when(filterChain.filter(exchange)).thenReturn(Mono.empty());

        // When
        Mono<Void> result = filter.filter(exchange, filterChain);

        // Then
        StepVerifier.create(result)
                .verifyComplete();

        HttpHeaders responseHeaders = exchange.getResponse().getHeaders();
        assertThat(responseHeaders.get("X-Trace-Id")).isNull();
        verify(filterChain).filter(exchange);
    }

    @Test
    void shouldContinueFilterChainOnError() {
        // Given
        when(currentTraceContext.context()).thenReturn(traceContext);
        when(traceContext.traceId()).thenReturn("trace-123");

        ServerWebExchange exchange = MockServerWebExchange.from(
                MockServerHttpRequest.get("/test").build());

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
    void shouldAddTraceIdHeaderForPostRequest() {
        // Given
        String expectedTraceId = "fedcba0987654321";
        when(currentTraceContext.context()).thenReturn(traceContext);
        when(traceContext.traceId()).thenReturn(expectedTraceId);

        ServerWebExchange exchange = MockServerWebExchange.from(
                MockServerHttpRequest.post("/api/resource")
                        .header("Content-Type", "application/json")
                        .build());

        when(filterChain.filter(exchange)).thenReturn(Mono.empty());

        // When
        Mono<Void> result = filter.filter(exchange, filterChain);

        // Then
        StepVerifier.create(result)
                .verifyComplete();

        HttpHeaders responseHeaders = exchange.getResponse().getHeaders();
        assertThat(responseHeaders.getFirst("X-Trace-Id")).isEqualTo(expectedTraceId);
    }
}
