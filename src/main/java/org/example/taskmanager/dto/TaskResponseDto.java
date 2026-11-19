package org.example.taskmanager.dto;

import org.example.taskmanager.model.TaskStatus;

public record TaskResponseDto(Long id, String title, UserDto assignee, String description, TaskStatus status) {
}
