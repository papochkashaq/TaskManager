package org.example.taskmanager.exception;

import org.springframework.http.HttpStatus;

public class UserNotFoundException extends RestException {
    public UserNotFoundException() {

        super("User was not found", HttpStatus.NOT_FOUND);
    }
}
