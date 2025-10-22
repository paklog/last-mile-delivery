package com.paklog.lastmile.domain.event;

import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class RouteOptimizedEvent extends DomainEvent {
    private final String routeId;
    private final double totalDistance;
    private final int optimizationScore;
    private final int stopCount;

    @Builder
    public RouteOptimizedEvent(String routeId, double totalDistance,
                              int optimizationScore, int stopCount) {
        super();
        this.routeId = routeId;
        this.totalDistance = totalDistance;
        this.optimizationScore = optimizationScore;
        this.stopCount = stopCount;
    }

    @Override
    public String getEventType() {
        return "RouteOptimized";
    }
}
