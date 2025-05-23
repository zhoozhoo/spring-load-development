package ca.zhoozhoo.loaddev.mcp.tools;

import java.util.List;

import org.springframework.ai.chat.model.ToolContext;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.stereotype.Service;

import ca.zhoozhoo.loaddev.mcp.config.ReactiveContextHolder;
import ca.zhoozhoo.loaddev.mcp.dto.LoadDto;
import ca.zhoozhoo.loaddev.mcp.service.LoadsService;
import lombok.extern.log4j.Log4j2;

@Service
@Log4j2
public class LoadTools {

    @Autowired
    private LoadsService loadsService;

    @Tool(description = "Find load by ID")
    public LoadDto getLoadById(@ToolParam(description = "Load ID") Long id) {
        return null;
    }

    @Tool(description = "Find all loads")
    public List<LoadDto> getLoads(ToolContext context) {
        log.info("getLoadsBlocking() called");
        var reactiveContext = ReactiveContextHolder.reactiveContext.get();
        if (reactiveContext != null) {
            var authentication = ReactiveSecurityContextHolder.getContext()
                    .map(SecurityContext::getAuthentication)
                    .contextWrite(reactiveContext)
                    .block();
            return loadsService.getAllLoads()
                    .collectList()
                    .contextWrite(reactiveContext)
                    .doOnNext(list -> log.info("Loaded {} items", list.size()))
                    .doOnError(e -> log.error("Error: {}", e.getMessage()))
                    .block();
        } else {
            throw new IllegalStateException("No reactive context available");
        }
    }
}
