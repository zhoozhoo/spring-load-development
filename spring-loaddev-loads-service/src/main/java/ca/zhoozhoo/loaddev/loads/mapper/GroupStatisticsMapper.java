package ca.zhoozhoo.loaddev.loads.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import ca.zhoozhoo.loaddev.loads.dto.GroupStatisticsDto;
import ca.zhoozhoo.loaddev.loads.dto.ShotDto;
import ca.zhoozhoo.loaddev.loads.model.GroupStatistics;
import ca.zhoozhoo.loaddev.loads.model.Shot;

@Mapper(componentModel = "spring")
public interface GroupStatisticsMapper {

    @Mapping(source = "group.date", target = "date")
    @Mapping(source = "group.powderCharge", target = "powderCharge")
    @Mapping(source = "group.targetRange", target = "targetRange")
    @Mapping(source = "group.groupSize", target = "groupSize")
    GroupStatisticsDto toDto(GroupStatistics statistics);

    @Mapping(source = "velocity", target = "velocity")
    ShotDto shotToShotDto(Shot shot);
}
