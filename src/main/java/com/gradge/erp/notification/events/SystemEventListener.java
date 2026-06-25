package com.gradge.erp.notification.events;

import com.gradge.erp.notification.service.RealtimeMessagingService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SystemEventListener {

    private final RealtimeMessagingService messagingService;

    @EventListener
    public void handle(SystemEvent event) {
        messagingService.sendToTenant(event);
    }
}
