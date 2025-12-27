package ca.zhoozhoo.loaddev.loads.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import ca.zhoozhoo.loaddev.loads.dto.GroupStatisticsDto;
import ca.zhoozhoo.loaddev.loads.dto.ShotDto;
import ca.zhoozhoo.loaddev.loads.model.GroupStatistics;
import ca.zhoozhoo.loaddev.loads.model.Shot;

/**
 * MapStruct mapper for converting between group statistics domain models and DTOs.
 * <p>
 * This mapper automatically generates implementation code for transforming
 * {@link GroupStatistics} entities into {@link GroupStatisticsDto} objects suitable
 * for API responses. It handles nested object mapping and field extraction from
 * the embedded Group entity. Since both source and target use javax.measure Quantity
 * objects, no unit conversion is needed - the Quantity objects are passed through directly.
 * </p>
 *
 * @author Zhubin Salehi
 */
@Mapper(componentModel = "spring")
public interface GroupStatisticsMapper {

    @Mapping(source = "group.date", target = "date")
    @Mapping(source = "group.powderCharge", target = "powderCharge")
    @Mapping(source = "group.targetRange", target = "targetRange")
    @Mapping(source = "group.groupSize", target = "groupSize")
    @Mapping(source = "averageVelocity", target = "averageVelocity")
    @Mapping(source = "standardDeviation", target = "standardDeviation")
    @Mapping(source = "extremeSpread", target = "extremeSpread")
    @Mapping(source = "shots", target = "shots")
    GroupStatisticsDto toDto(GroupStatistics statistics);

    @Mapping(source = "velocity", target = "velocity")
    ShotDto shotToShotDto(Shot shot);
}
