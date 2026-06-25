package com.gradge.erp.notification.events;

import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class SystemEvent {

    private SystemEventType type;

    private UUID tenantId;

    private String message;

    private Object data;

    private LocalDateTime timestamp;
}
