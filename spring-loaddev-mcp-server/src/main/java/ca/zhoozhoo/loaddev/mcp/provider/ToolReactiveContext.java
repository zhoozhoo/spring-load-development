package ca.zhoozhoo.loaddev.mcp.provider;

import org.springframework.ai.model.tool.internal.ToolCallReactiveContextHolder;

import lombok.extern.log4j.Log4j2;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Utilities for applying MCP tool reactive context (security, tracing, etc.)
 * captured by ToolCallReactiveContextHolder to Reactor types.
 */
@Log4j2
public final class ToolReactiveContext {

    private ToolReactiveContext() {
    }

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
