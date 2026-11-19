package org.example.taskmanager.event;

import org.example.taskmanager.model.TaskEventType;

import java.time.LocalDateTime;
import java.util.UUID;

public record TaskEvent(
        UUID eventId,
        TaskEventType eventType,
        Long taskId,
        Long assigneeId,
        LocalDateTime occurredAt
) {
}
