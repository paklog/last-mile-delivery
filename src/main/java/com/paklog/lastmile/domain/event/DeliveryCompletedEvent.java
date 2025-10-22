package com.paklog.lastmile.domain.event;

import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.Instant;

@Data
@EqualsAndHashCode(callSuper = true)
public class DeliveryCompletedEvent extends DomainEvent {
    private final String routeId;
    private final String stopId;
    private final String customerId;
    private final Instant completedAt;
    private final int attemptNumber;

    @Builder
    public DeliveryCompletedEvent(String routeId, String stopId, String customerId,
                                 Instant completedAt, int attemptNumber) {
        super();
        this.routeId = routeId;
        this.stopId = stopId;
        this.customerId = customerId;
        this.completedAt = completedAt;
        this.attemptNumber = attemptNumber;
    }

    @Override
    public String getEventType() {
        return "DeliveryCompleted";
    }
}
