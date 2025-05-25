package ca.zhoozhoo.loaddev.mcp.tools;

import static reactor.core.publisher.Mono.error;
import static reactor.core.publisher.Mono.just;

import java.util.List;

import org.springframework.ai.chat.model.ToolContext;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Service;

import ca.zhoozhoo.loaddev.mcp.config.McpToolRegistrationConfig.ReactiveContextHolder;
import ca.zhoozhoo.loaddev.mcp.dto.LoadDto;
import ca.zhoozhoo.loaddev.mcp.service.LoadsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import reactor.util.context.ContextView;

@Service
@Log4j2
@RequiredArgsConstructor
public class LoadTools {
    private final LoadsService loadsService;

    @Tool(description = "Find a specific load by its unique identifier", name = "getLoadById")
    public LoadDto getLoadById(
            @ToolParam(description = "Numeric ID of the load to retrieve") Long id,
            ToolContext context) {
        log.debug("Retrieving load with ID: {}", id);
        ContextView reactiveContext = getReactiveContext();

        try {
            return just(id)
                    .flatMap(loadId -> loadsService.getLoadById(loadId)
                            .switchIfEmpty(error(
                                    new IllegalArgumentException("Load not found with ID: " + id))))
                    .contextWrite(ctx -> ctx.putAll(reactiveContext))
                    .doOnSuccess(load -> log.debug("Successfully retrieved load: {}", load))
                    .doOnError(e -> log.error("Error retrieving load {}: {}", id, e.getMessage()))
                    .block();
        } catch (Exception e) {
            throw handleException("Failed to retrieve load", e);
        }
    }

    @Tool(description = "Retrieve all available loads in the system", name = "getLoads")
    public List<LoadDto> getLoads(ToolContext context) {
        log.debug("Retrieving all loads");
        ContextView reactiveContext = getReactiveContext();

        try {
            return loadsService.getLoads()
                    .contextWrite(ctx -> ctx.putAll(reactiveContext))
                    .collectList()
                    .doOnSuccess(list -> log.debug("Successfully retrieved {} loads", list.size()))
                    .doOnError(e -> log.error("Error retrieving loads: {}", e.getMessage()))
                    .block();
        } catch (Exception e) {
            throw handleException("Failed to retrieve loads", e);
        }
    }

    private ContextView getReactiveContext() {
        var reactiveContext = ReactiveContextHolder.reactiveContext.get();
        if (reactiveContext == null) {
            throw new IllegalStateException("No reactive context available");
        }
        return reactiveContext;
    }

    private RuntimeException handleException(String message, Exception e) {
        if (e instanceof IllegalStateException) {
            return (IllegalStateException) e;
        }
        if (e.getCause() instanceof SecurityException) {
            return new IllegalStateException("Authentication failed: " + e.getMessage(), e);
        }
        log.error(message, e);
        return new IllegalStateException(message + ": " + e.getMessage(), e);
    }
}
