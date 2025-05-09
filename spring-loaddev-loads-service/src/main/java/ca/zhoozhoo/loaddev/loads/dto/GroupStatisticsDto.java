package ca.zhoozhoo.loaddev.loads.dto;

import java.util.List;

public record GroupStatisticsDto(

        GroupDto group,

        int shotCount,

        double averageVelocity,

        double standardDeviation,

        double extremeSpread,
        
        List<ShotDto> shots) {
}