package com.paklog.lastmile.domain.event;

import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class DeliveryFailedEvent extends DomainEvent {
    private final String routeId;
    private final String stopId;
    private final String customerId;
    private final String reason;
    private final int attemptsCount;

    @Builder
    public DeliveryFailedEvent(String routeId, String stopId, String customerId,
                              String reason, int attemptsCount) {
        super();
        this.routeId = routeId;
        this.stopId = stopId;
        this.customerId = customerId;
        this.reason = reason;
        this.attemptsCount = attemptsCount;
    }

    @Override
    public String getEventType() {
        return "DeliveryFailed";
    }
}
