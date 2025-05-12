package ca.zhoozhoo.loaddev.mcp.tools;

import java.util.List;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Service;

import ca.zhoozhoo.loaddev.mcp.dto.LoadDto;

@Service
public class LoadTools {

    @Tool(description = "Find load by ID")
    public LoadDto getLoadById(@ToolParam(description = "Load ID") Long id) {
        return null;
    }

    @Tool(description = "Find all loads")
    public List<LoadDto> getLoads() {
        return null;
    }
}
