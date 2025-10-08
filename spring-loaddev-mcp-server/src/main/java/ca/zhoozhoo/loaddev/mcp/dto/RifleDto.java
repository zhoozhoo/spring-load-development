package ca.zhoozhoo.loaddev.mcp.dto;

/**
 * Data Transfer Object representing a rifle.
 * <p>
 * Contains comprehensive information about a rifle including caliber, barrel
 * specifications, twist rate, and scope details.
 * <p>
 * This record is used for transferring rifle data between the MCP server and
 * backend microservices.
 * 
 * @author Zhubin Salehi
 */
public record RifleDto(

        Long id,

        String name,

        String description,

        String measurementUnits,

        String caliber,

        Double barrelLength,

        String barrelContour,

        String twistRate,

        String rifling,

        String freeBore) {
}
