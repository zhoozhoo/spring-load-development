package ca.zhoozhoo.loaddev.mcp.config;

import org.springframework.ai.mcp.McpToolUtils;
import org.springframework.ai.mcp.server.autoconfigure.McpServerProperties;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.util.MimeType;

import io.modelcontextprotocol.server.McpAsyncServer;
import io.modelcontextprotocol.server.McpServerFeatures;
import io.modelcontextprotocol.server.McpServerFeatures.AsyncToolSpecification;
import io.modelcontextprotocol.server.McpSyncServerExchange;
import lombok.extern.log4j.Log4j2;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

@Component
@Log4j2
public class McpAsyncServerConfig {

    @Autowired
    private McpAsyncServer mcpAsyncServer;

    @Autowired
    private McpServerProperties serverProperties;

    @Autowired
    private ToolCallbackProvider callbackProvider;

    @EventListener(ApplicationReadyEvent.class)
    public void afterPropertiesSet() {
        log.info("Starting afterPropertiesSet: ToolCallbackProvider={}, McpAsyncServer={}, McpServerProperties={}", callbackProvider, mcpAsyncServer, serverProperties);
        // For each tool, remove and re-add as AsyncToolSpecification
        for (ToolCallback toolCallback : callbackProvider.getToolCallbacks()) {
            String toolName = toolCallback.getToolDefinition().name();
            log.info("Processing tool: {}", toolName);
            mcpAsyncServer.removeTool(toolName);
            MimeType mimeType = (serverProperties.getToolResponseMimeType().containsKey(toolName))
                    ? MimeType.valueOf(serverProperties.getToolResponseMimeType().get(toolName))
                    : null;
            log.info("Using mimeType={} for tool {}", mimeType, toolName);
            AsyncToolSpecification asyncTool = toAsyncToolSpecification(toolCallback, mimeType);
            log.info("Adding AsyncToolSpecification for tool: {}", toolName);
            mcpAsyncServer.addTool(asyncTool);
        }
        log.info("afterPropertiesSet completed");
    }

    private AsyncToolSpecification toAsyncToolSpecification(ToolCallback toolCallback, MimeType mimeType) {
        log.info("Creating AsyncToolSpecification for tool: {} with mimeType={}", toolCallback.getToolDefinition().name(), mimeType);
        McpServerFeatures.SyncToolSpecification syncToolSpecification = McpToolUtils
                .toSyncToolSpecification(toolCallback, mimeType);
        log.debug("SyncToolSpecification created for tool: {}", toolCallback.getToolDefinition().name());
        return new AsyncToolSpecification(syncToolSpecification.tool(),
                (exchange, map) -> Mono.deferContextual(
                        c -> Mono.fromCallable(() -> {
                            log.debug("AsyncToolSpecification.call() invoked for tool: {}", toolCallback.getToolDefinition().name());
                            try {
                                ReactiveContextHolder.reactiveContext.set(c);
                                log.trace("Reactive context set for tool: {}", toolCallback.getToolDefinition().name());
                                return syncToolSpecification.call().apply(new McpSyncServerExchange(exchange), map);
                            } finally {
                                ReactiveContextHolder.reactiveContext.remove();
                                log.trace("Reactive context removed for tool: {}", toolCallback.getToolDefinition().name());
                            }
                        }))
                        .subscribeOn(Schedulers.boundedElastic()));
    }
}
