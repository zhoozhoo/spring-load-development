package ca.zhoozhoo.loaddev.mcp.dto;

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
