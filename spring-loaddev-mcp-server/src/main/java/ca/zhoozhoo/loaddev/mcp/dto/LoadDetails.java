package ca.zhoozhoo.loaddev.mcp.dto;

import java.util.List;

/**
 * Aggregated data structure combining load, rifle, and shooting group information.
 *
 * <p>This record provides a comprehensive view of a specific load's performance
 * by combining the load specifications, the rifle used, and all shooting groups
 * associated with testing that load. This is the primary data structure returned
 * by the {@code getLoadDetailsById} MCP tool.
 *
 * @author Zhubin Salehi
 */
public record LoadDetails(

        LoadDto load,

        RifleDto rifle,

        List<GroupDto> groups) {
}
