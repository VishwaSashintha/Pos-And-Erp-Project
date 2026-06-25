package com.gradge.erp.notification.events;

import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EventPublisherService {

    private final ApplicationEventPublisher publisher;

    public void publish(SystemEvent event) {
        publisher.publishEvent(event);
    }
}
