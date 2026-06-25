package com.gradge.erp.shared.event;

public interface EventPublisherPort {

    void publish(DomainEvent event);
}
