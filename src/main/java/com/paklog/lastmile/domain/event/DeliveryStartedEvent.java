package com.paklog.lastmile.domain.event;

import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.Instant;

@Data
@EqualsAndHashCode(callSuper = true)
public class DeliveryStartedEvent extends DomainEvent {
    private final String routeId;
    private final String driverId;
    private final String vehicleId;
    private final Instant startedAt;

    @Builder
    public DeliveryStartedEvent(String routeId, String driverId, String vehicleId, Instant startedAt) {
        super();
        this.routeId = routeId;
        this.driverId = driverId;
        this.vehicleId = vehicleId;
        this.startedAt = startedAt;
    }

    @Override
    public String getEventType() {
        return "DeliveryStarted";
    }
}
