package com.project.ArtRari.exception;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;

@RestControllerAdvice
public class ArtrariErrorAdvice {
    @ExceptionHandler(ArtrariException.class)
    public ResponseEntity<?> artrariException(ArtrariException e) {
        ErrorResponse errorResponse = new ErrorResponse(
                e.getStatus().value(),
                e.getStatus().name(),
                e.getMessage(),
                Instant.now()
        );
        return ResponseEntity.status(e.getStatus()).body(errorResponse);
    }
}
