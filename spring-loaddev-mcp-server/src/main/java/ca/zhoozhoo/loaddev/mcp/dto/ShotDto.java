package ca.zhoozhoo.loaddev.mcp.dto;

/**
 * Data Transfer Object representing a single shot measurement.
 *
 * <p>This record captures the velocity measurement for an individual shot
 * within a shooting group. Multiple shots are aggregated to calculate
 * group-level ballistic statistics.
 *
 * @author Zhubin Salehi
 */
public record ShotDto(

        Integer velocity) {
}
