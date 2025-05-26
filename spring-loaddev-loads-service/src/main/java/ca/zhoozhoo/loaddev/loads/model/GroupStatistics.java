package ca.zhoozhoo.loaddev.loads.model;

import java.util.List;

public record GroupStatistics(
        Group group,

        double averageVelocity,

        double standardDeviation,

        double extremeSpread,

        List<Shot> shots) {

    // Custom constructor that rounds double values to one decimal point
    public GroupStatistics {
        averageVelocity = Math.round(averageVelocity * 10.0) / 10.0;
        standardDeviation = Math.round(standardDeviation * 10.0) / 10.0;
        extremeSpread = Math.round(extremeSpread * 10.0) / 10.0;
    }
}
