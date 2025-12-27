package ca.zhoozhoo.loaddev.mcp.dto;

import java.util.List;

/**
 * Aggregated load performance data combining load, rifle, and shooting groups.
 * <p>
 * Primary data structure returned by {@code getLoadDetailsById} MCP tool.
 *
 * @author Zhubin Salehi
 */
public record LoadDetails(

        LoadDto load,

        RifleDto rifle,

        List<GroupDto> groups) {

    /**
     * Compact constructor that creates defensive copies of mutable collections.
     */
    public LoadDetails {
        groups = groups != null ? List.copyOf(groups) : List.of();
    }
}
