package ca.zhoozhoo.loaddev.loads.model;

import java.util.List;

public record GroupStatistics(
        Group group,
        int shotCount,
        double averageVelocity,
        double standardDeviation,
        double extremeSpread,
        List<Shot> shots) {
}
