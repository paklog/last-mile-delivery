package com.paklog.lastmile.domain.event;

import lombok.Data;

import java.time.Instant;

@Data
public abstract class DomainEvent {
    private final String eventId;
    private final Instant occurredAt;

    protected DomainEvent() {
        this.eventId = java.util.UUID.randomUUID().toString();
        this.occurredAt = Instant.now();
    }

    public abstract String getEventType();
}
