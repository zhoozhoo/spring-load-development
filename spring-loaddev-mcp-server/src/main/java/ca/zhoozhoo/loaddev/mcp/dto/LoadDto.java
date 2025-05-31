package ca.zhoozhoo.loaddev.mcp.dto;

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