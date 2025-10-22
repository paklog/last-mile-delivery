package com.paklog.lastmile.domain.event;

import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class RouteCreatedEvent extends DomainEvent {
    private final String routeId;
    private final String routeNumber;
    private final String vehicleId;
    private final String driverId;
    private final int totalStops;

    @Builder
    public RouteCreatedEvent(String routeId, String routeNumber, String vehicleId,
                            String driverId, int totalStops) {
        super();
        this.routeId = routeId;
        this.routeNumber = routeNumber;
        this.vehicleId = vehicleId;
        this.driverId = driverId;
        this.totalStops = totalStops;
    }

    @Override
    public String getEventType() {
        return "RouteCreated";
    }
}
