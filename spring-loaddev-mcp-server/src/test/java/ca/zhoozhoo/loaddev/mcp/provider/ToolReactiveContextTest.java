package ca.zhoozhoo.loaddev.mcp.provider;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.ai.model.tool.internal.ToolCallReactiveContextHolder;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import reactor.util.context.Context;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static reactor.util.context.Context.of;

/**
 * Unit tests for {@link ToolReactiveContext}.
 * Tests context propagation for Mono and Flux publishers.
 */
class ToolReactiveContextTest {

    @AfterEach
    void cleanup() {
        // Clear context holder after each test
        ToolCallReactiveContextHolder.clearContext();
    }

    @Test
    void applyToMonoWithContext() {
        // Setup: set reactive context in holder
        ToolCallReactiveContextHolder.setContext(of("key1", "value1", "key2", 42));

        var mono = Mono.just("test")
                .transformDeferredContextual((data, ctx) -> {
                    // Verify context is present
                    assertThat(ctx.hasKey("key1")).isTrue();
                    assertThat((Object) ctx.get("key1")).isEqualTo("value1");
                    assertThat(ctx.hasKey("key2")).isTrue();
                    assertThat((Object) ctx.get("key2")).isEqualTo(42);
                    return data;
                });

        var result = ToolReactiveContext.applyTo(mono);

        StepVerifier.create(result)
                .expectNext("test")
                .verifyComplete();
    }

    @Test
    void applyToMonoWithoutContext() {
        // No context set - should return mono unchanged
        ToolCallReactiveContextHolder.clearContext();

        var mono = Mono.just("test");
        var result = ToolReactiveContext.applyTo(mono);

        assertThat(result).isSameAs(mono);

        StepVerifier.create(result)
                .expectNext("test")
                .verifyComplete();
    }

    @Test
    void applyToMonoWithEmptyContext() {
        // Empty context - should return mono unchanged
        ToolCallReactiveContextHolder.setContext(Context.empty());

        var mono = Mono.just("test");
        var result = ToolReactiveContext.applyTo(mono);

        assertThat(result).isSameAs(mono);

        StepVerifier.create(result)
                .expectNext("test")
                .verifyComplete();
    }

    @Test
    void applyToFluxWithContext() {
        // Setup: set reactive context in holder
        ToolCallReactiveContextHolder.setContext(of("user", "testUser", "tenant", "testTenant"));

        var flux = Flux.just("a", "b", "c")
                .transformDeferredContextual((data, ctx) -> {
                    // Verify context is present
                    assertThat(ctx.hasKey("user")).isTrue();
                    assertThat((Object) ctx.get("user")).isEqualTo("testUser");
                    assertThat(ctx.hasKey("tenant")).isTrue();
                    assertThat((Object) ctx.get("tenant")).isEqualTo("testTenant");
                    return data;
                });

        var result = ToolReactiveContext.applyTo(flux);

        StepVerifier.create(result)
                .expectNext("a", "b", "c")
                .verifyComplete();
    }

    @Test
    void applyToFluxWithoutContext() {
        // No context set - should return flux unchanged
        ToolCallReactiveContextHolder.clearContext();

        var flux = Flux.just("a", "b", "c");
        var result = ToolReactiveContext.applyTo(flux);

        assertThat(result).isSameAs(flux);

        StepVerifier.create(result)
                .expectNext("a", "b", "c")
                .verifyComplete();
    }

    @Test
    void applyToFluxWithEmptyContext() {
        // Empty context - should return flux unchanged
        ToolCallReactiveContextHolder.setContext(Context.empty());

        var flux = Flux.just("a", "b", "c");
        var result = ToolReactiveContext.applyTo(flux);

        assertThat(result).isSameAs(flux);

        StepVerifier.create(result)
                .expectNext("a", "b", "c")
                .verifyComplete();
    }

    @Test
    void applyToMonoPreservesErrors() {
        ToolCallReactiveContextHolder.setContext(of("key", "value"));

        var mono = Mono.<String>error(new RuntimeException("test error"));
        var result = ToolReactiveContext.applyTo(mono);

        StepVerifier.create(result)
                .expectError(RuntimeException.class)
                .verify();
    }

    @Test
    void applyToFluxPreservesErrors() {
        ToolCallReactiveContextHolder.setContext(of("key", "value"));

        var flux = Flux.<String>error(new RuntimeException("test error"));
        var result = ToolReactiveContext.applyTo(flux);

        StepVerifier.create(result)
                .expectError(RuntimeException.class)
                .verify();
    }

    @Test
    void contextPropagatesForTiming() {
        ToolCallReactiveContextHolder.setContext(of("trace", "12345"));

        var mono = Mono.just("data")
                .flatMap(d -> Mono.deferContextual(ctx -> {
                    // Context should be accessible in nested reactive operations
                    assertThat(ctx.hasKey("trace")).isTrue();
                    return Mono.just(d + "-" + ctx.get("trace"));
                }));

        var result = ToolReactiveContext.applyTo(mono);

        StepVerifier.create(result)
                .expectNext("data-12345")
                .verifyComplete();
    }

    @Test
    void contextPropagatesInFlux() {
        ToolCallReactiveContextHolder.setContext(of("correlation", "abc-123"));

        var flux = Flux.just("x", "y")
                .flatMap(item -> Mono.deferContextual(ctx -> {
                    // Context should propagate through flux operations
                    assertThat(ctx.hasKey("correlation")).isTrue();
                    return Mono.just(item + "-" + ctx.get("correlation"));
                }));

        var result = ToolReactiveContext.applyTo(flux);

        StepVerifier.create(result)
                .expectNext("x-abc-123", "y-abc-123")
                .verifyComplete();
    }

    @Test
    void constructorThrowsException() {
        assertThatThrownBy(() -> {
            var constructor = ToolReactiveContext.class.getDeclaredConstructor();
            constructor.setAccessible(true);
            constructor.newInstance();
        })
                .hasCauseInstanceOf(UnsupportedOperationException.class)
                .hasStackTraceContaining("Utility class cannot be instantiated");
    }
}
