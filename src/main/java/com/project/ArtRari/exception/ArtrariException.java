package com.project.ArtRari.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class ArtrariException extends RuntimeException {
    private final HttpStatus status;

    public ArtrariException(HttpStatus status, String message) {
        super(message);
        this.status = status;
    }
}
