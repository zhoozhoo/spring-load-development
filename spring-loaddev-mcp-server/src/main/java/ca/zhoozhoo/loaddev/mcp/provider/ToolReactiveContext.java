package ca.zhoozhoo.loaddev.mcp.provider;

import org.springframework.ai.model.tool.internal.ToolCallReactiveContextHolder;

import lombok.extern.log4j.Log4j2;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Utilities for applying MCP tool reactive context (security, tracing, etc.)
 * captured by ToolCallReactiveContextHolder to Reactor types.
 * <p>
 * This utility propagates the reactive context (including security authentication)
 * from the MCP framework layer to downstream service calls. Without this propagation,
 * {@link org.springframework.security.core.context.ReactiveSecurityContextHolder}
 * would not have access to authentication information in service methods.
 * <p>
 * Usage pattern:
 * <pre>{@code
 * return ToolReactiveContext.applyTo(
 *     service.getData()
 *         .map(data -> process(data))
 * );
 * }</pre>
 *
 * @author Zhubin Salehi
 * @see org.springframework.ai.model.tool.internal.ToolCallReactiveContextHolder
 * @see reactor.util.context.Context
 */
@Log4j2
public final class ToolReactiveContext {

    /**
     * Private constructor to prevent instantiation of utility class.
     */
    private ToolReactiveContext() {
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }

    /**
     * Applies the MCP tool reactive context to a Mono publisher.
     * <p>
     * Retrieves the reactive context from {@link ToolCallReactiveContextHolder}
     * and writes it to the Mono's context chain, enabling context propagation
     * through reactive operators.
     *
     * @param <T> the type of data emitted by the Mono
     * @param mono the source Mono to apply context to
     * @return the same Mono with context applied, or unchanged if no context exists
     */
    public static <T> Mono<T> applyTo(Mono<T> mono) {
        var reactiveContext = ToolCallReactiveContextHolder.getContext();
        if (reactiveContext == null || reactiveContext.isEmpty()) {
            log.debug("No reactive context to apply to Mono");
            return mono;
        }
        if (log.isDebugEnabled()) {
            log.debug("Applying reactive context to Mono (size={})", reactiveContext.size());
            reactiveContext.forEach((key, value) ->
                log.debug("  Context key: {}, value type: {}",
                    key, value != null ? value.getClass().getName() : "null")
            );
        }
        return mono.contextWrite(ctx -> ctx.putAll(reactiveContext));
    }

    /**
     * Applies the MCP tool reactive context to a Flux publisher.
     * <p>
     * Retrieves the reactive context from {@link ToolCallReactiveContextHolder}
     * and writes it to the Flux's context chain, enabling context propagation
     * through reactive operators.
     *
     * @param <T> the type of data emitted by the Flux
     * @param flux the source Flux to apply context to
     * @return the same Flux with context applied, or unchanged if no context exists
     */
    public static <T> Flux<T> applyTo(Flux<T> flux) {
        var reactiveContext = ToolCallReactiveContextHolder.getContext();
        if (reactiveContext == null || reactiveContext.isEmpty()) {
            log.debug("No reactive context to apply to Flux");
            return flux;
        }
        if (log.isDebugEnabled()) {
            log.debug("Applying reactive context to Flux (size={})", reactiveContext.size());
            reactiveContext.forEach((key, value) ->
                log.debug("  Context key: {}, value type: {}",
                    key, value != null ? value.getClass().getName() : "null")
            );
        }
        return flux.contextWrite(ctx -> ctx.putAll(reactiveContext));
    }
}
