package com.gradge.erp.notification.service;

import com.gradge.erp.notification.events.SystemEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RealtimeMessagingService {

    private final SimpMessagingTemplate messagingTemplate;

    public void sendToTenant(SystemEvent event) {

        String topic = "/topic/tenant/" + event.getTenantId();

        messagingTemplate.convertAndSend(topic, event);
    }
}
