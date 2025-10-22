package com.paklog.lastmile.domain.event;

import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.Instant;

@Data
@EqualsAndHashCode(callSuper = true)
public class RouteCompletedEvent extends DomainEvent {
    private final String routeId;
    private final Instant completedAt;
    private final int totalStops;
    private final int successfulStops;
    private final int failedStops;
    private final double totalDistanceKm;

    @Builder
    public RouteCompletedEvent(String routeId, Instant completedAt, int totalStops,
                              int successfulStops, int failedStops, double totalDistanceKm) {
        super();
        this.routeId = routeId;
        this.completedAt = completedAt;
        this.totalStops = totalStops;
        this.successfulStops = successfulStops;
        this.failedStops = failedStops;
        this.totalDistanceKm = totalDistanceKm;
    }

    @Override
    public String getEventType() {
        return "RouteCompleted";
    }
}
