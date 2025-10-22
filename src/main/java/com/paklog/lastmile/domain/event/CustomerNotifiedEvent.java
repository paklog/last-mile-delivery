package com.paklog.lastmile.domain.event;

import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class CustomerNotifiedEvent extends DomainEvent {
    private final String routeId;
    private final String notificationType;
    private final String message;

    @Builder
    public CustomerNotifiedEvent(String routeId, String notificationType, String message) {
        super();
        this.routeId = routeId;
        this.notificationType = notificationType;
        this.message = message;
    }

    @Override
    public String getEventType() {
        return "CustomerNotified";
    }
}
