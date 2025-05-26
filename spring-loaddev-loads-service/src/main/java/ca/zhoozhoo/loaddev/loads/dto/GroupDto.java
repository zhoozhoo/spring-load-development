package ca.zhoozhoo.loaddev.loads.dto;

import java.time.LocalDate;
import java.util.List;

public record GroupDto(

        LocalDate date,

        Double powderCharge,

        Integer targetRange,

        Double groupSize,

        double averageVelocity,

        double standardDeviation,

        double extremeSpread,

        List<ShotDto> shots) {
}
