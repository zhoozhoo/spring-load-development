package ca.zhoozhoo.loaddev.loads.dto;

/**
 * Data Transfer Object for shot velocity data.
 * <p>
 * This lightweight DTO represents a single shot's velocity measurement,
 * used primarily in group statistics responses to provide shot-level detail
 * without exposing internal identifiers or relationships.
 * </p>
 *
 * @author Zhubin Salehi
 */
public record ShotDto(

        double velocity) {
}