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
    public void replacesAsyncToolSpecifications() {
        // For each tool, remove and re-add as AsyncToolSpecification
        for (ToolCallback toolCallback : callbackProvider.getToolCallbacks()) {
            var toolName = toolCallback.getToolDefinition().name();
            mcpAsyncServer.removeTool(toolName).subscribe();

            var mimeType = (serverProperties.getToolResponseMimeType().containsKey(toolName))
                    ? MimeType.valueOf(serverProperties.getToolResponseMimeType().get(toolName))
                    : null;
            mcpAsyncServer.addTool(toAsyncToolSpecification(toolCallback, mimeType)).subscribe();
        }
    }

    private AsyncToolSpecification toAsyncToolSpecification(ToolCallback toolCallback, MimeType mimeType) {
        var syncToolSpecification = McpToolUtils.toSyncToolSpecification(toolCallback, mimeType);
        return new AsyncToolSpecification(syncToolSpecification.tool(),
                (exchange, map) -> Mono.deferContextual(
                        c -> Mono.fromCallable(() -> {
                            try {
                                ReactiveContextHolder.reactiveContext.set(c);
                                return syncToolSpecification.call().apply(new McpSyncServerExchange(exchange), map);
                            } finally {
                                ReactiveContextHolder.reactiveContext.remove();
                            }
                        }))
                        .subscribeOn(Schedulers.boundedElastic()));
    }
}
