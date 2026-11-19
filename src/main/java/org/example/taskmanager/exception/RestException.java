package org.example.taskmanager.exception;

import org.springframework.http.HttpStatus;

public abstract class RestException extends RuntimeException {

    private final HttpStatus httpStatus;

    protected RestException(String message, HttpStatus httpStatus) {
        super(message);
        this.httpStatus = httpStatus;
    }

    public HttpStatus getHttpStatus() {
        return httpStatus;
    }
}
