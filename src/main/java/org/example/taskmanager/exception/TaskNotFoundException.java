package org.example.taskmanager.exception;

import org.springframework.http.HttpStatus;

public class TaskNotFoundException extends RestException {
    public TaskNotFoundException() {
        super("Task was not found", HttpStatus.NOT_FOUND);
    }
}
