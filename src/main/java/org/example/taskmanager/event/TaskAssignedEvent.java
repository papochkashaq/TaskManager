package org.example.taskmanager.event;

public record TaskAssignedEvent(Long taskId, Long assigneeId) {
}
