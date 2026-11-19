package org.example.taskmanager.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.example.taskmanager.dto.TaskCreateRequestDto;
import org.example.taskmanager.dto.TaskResponseDto;
import org.example.taskmanager.dto.UserDto;
import org.example.taskmanager.model.TaskStatus;
import org.example.taskmanager.service.TaskService;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/task")
public class TaskController {

    private final TaskService taskService;

    public TaskController(TaskService taskService) {
        this.taskService = taskService;
    }

    @GetMapping("/{id}")
    public ResponseEntity<TaskResponseDto> getTaskById(@PathVariable Long id) {
        TaskResponseDto taskResponseDto = taskService.getTaskById(id);
        return ResponseEntity.ok(taskResponseDto);
    }

    @GetMapping
    public ResponseEntity<Page<TaskResponseDto>> getTasks(@RequestParam(defaultValue = "0") int page,
                                                          @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size) {
        Page<TaskResponseDto> taskDtoPage = taskService.getTasks(page, size);
        return ResponseEntity.ok(taskDtoPage);
    }

    @PostMapping
    public ResponseEntity<TaskResponseDto> createTask(@Valid @RequestBody TaskCreateRequestDto createRequestDto) {
        TaskResponseDto responseDto = taskService.createTask(createRequestDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(responseDto);
    }

    @PatchMapping("/{id}/assignee")
    public ResponseEntity<TaskResponseDto> assignUserToTask(@PathVariable Long id, @Valid @RequestBody UserDto userDto) {
        TaskResponseDto taskResponseDto = taskService.assignUserToTask(id, userDto);
        return ResponseEntity.ok(taskResponseDto);
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<TaskResponseDto> changeTaskStatus(@PathVariable Long id, @RequestParam TaskStatus status) {
        TaskResponseDto taskResponseDto = taskService.changeTaskStatus(id, status);
        return ResponseEntity.ok(taskResponseDto);
    }

}
