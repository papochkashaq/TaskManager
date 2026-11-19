package org.example.taskmanager.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record TaskCreateRequestDto(
        @NotBlank
        @Size(max = 128)
        String title,
        @Size(max = 2000, message = "Description must not exceed 2000 characters")
        String description) {
}
