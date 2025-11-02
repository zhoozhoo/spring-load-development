package ca.zhoozhoo.loaddev.mcp.dto;

/**
 * Data Transfer Object representing a reloading load.
 * <p>
 * Contains comprehensive information about a specific ammunition load including
 * powder details, bullet specifications, cartridge information, and associated
 * rifle reference.
 * <p>
 * This record is used for transferring load data between the MCP server and
 * backend microservices.
 * 
 * @author Zhubin Salehi
 */
public record LoadDto(

        Long id,

        String name,

        String description,

        String measurementUnits,

        String powderManufacturer,

        String powderType,

        String bulletManufacturer,

        String bulletType,

        Double bulletWeight,

        String primerManufacturer,

        String primerType,

        Double distanceFromLands,

        Double caseOverallLength,

        Double neckTension,

        Long rifleId) {
}