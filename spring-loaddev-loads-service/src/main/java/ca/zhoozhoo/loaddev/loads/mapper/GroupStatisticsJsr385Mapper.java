package ca.zhoozhoo.loaddev.loads.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import ca.zhoozhoo.loaddev.loads.dto.GroupStatisticsJsr385Dto;
import ca.zhoozhoo.loaddev.loads.dto.ShotJsr385Dto;
import ca.zhoozhoo.loaddev.loads.model.GroupStatisticsJsr385;
import ca.zhoozhoo.loaddev.loads.model.ShotJsr385;

/**
 * MapStruct mapper for converting between JSR-385 group statistics domain models and DTOs.
 * <p>
 * This mapper automatically generates implementation code for transforming
 * {@link GroupStatisticsJsr385} entities into {@link GroupStatisticsJsr385Dto} objects suitable
 * for API responses. It handles nested object mapping and field extraction from
 * the embedded GroupJsr385 entity. Since both source and target use javax.measure Quantity
 * objects, no unit conversion is needed - the Quantity objects are passed through directly.
 * </p>
 *
 * @author Zhubin Salehi
 */
@Mapper(componentModel = "spring")
public interface GroupStatisticsJsr385Mapper {

    @Mapping(source = "group.date", target = "date")
    @Mapping(source = "group.powderCharge", target = "powderCharge")
    @Mapping(source = "group.targetRange", target = "targetRange")
    @Mapping(source = "group.groupSize", target = "groupSize")
    @Mapping(source = "averageVelocity", target = "averageVelocity")
    @Mapping(source = "standardDeviation", target = "standardDeviation")
    @Mapping(source = "extremeSpread", target = "extremeSpread")
    @Mapping(source = "shots", target = "shots")
    GroupStatisticsJsr385Dto toDto(GroupStatisticsJsr385 statistics);

    @Mapping(source = "velocity", target = "velocity")
    ShotJsr385Dto shotToShotDto(ShotJsr385 shot);
}
