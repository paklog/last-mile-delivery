package com.paklog.lastmile.application.port.out;

import com.paklog.lastmile.domain.event.DomainEvent;

public interface PublishEventPort {

    void publish(DomainEvent event);
}
