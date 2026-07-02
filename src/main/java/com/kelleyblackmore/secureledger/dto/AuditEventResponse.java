package com.kelleyblackmore.secureledger.dto;

import com.kelleyblackmore.secureledger.entity.AuditEvent;

import java.time.Instant;

public record AuditEventResponse(
        Long id,
        String actor,
        String action,
        String entityType,
        String entityId,
        Instant timestamp,
        String detail
) {
    public static AuditEventResponse from(AuditEvent event) {
        return new AuditEventResponse(
                event.getId(),
                event.getActor(),
                event.getAction(),
                event.getEntityType(),
                event.getEntityId(),
                event.getTimestamp(),
                event.getDetail()
        );
    }
}
