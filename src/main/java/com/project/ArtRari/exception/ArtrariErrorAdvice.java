package com.project.ArtRari.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
@Slf4j
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

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<?> argumentNotValid(MethodArgumentNotValidException e) {
        Map<String, String> errors = new HashMap<>();
        e.getBindingResult().getFieldErrors().forEach(error -> errors.put(error.getField(), error.getDefaultMessage()));
        MethodArgumentNotValidErrorResponse errorResponse = new MethodArgumentNotValidErrorResponse(
                400,
                "BAD_REQUEST",
                errors,
                Instant.now()
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }

    @ExceptionHandler(ObjectOptimisticLockingFailureException.class)
    public ResponseEntity<?> objectOptimisticLockingFailureException(ObjectOptimisticLockingFailureException e) {
        log.warn("Конфлікт версій (Optimistic Lock): {}", e.getMessage());
        ErrorResponse errorResponse = new ErrorResponse(
                409,
                "CONFLICT",
                "Дані було змінено. Будь ласка, оновіть сторінку",
                Instant.now()
        );
        return ResponseEntity.status(HttpStatus.CONFLICT).body(errorResponse);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<?> dataIntegrityViolationException(DataIntegrityViolationException e) {
        log.error("Помилка цілісності даних БД: {}", e.getMessage(), e);
        ErrorResponse errorResponse = new ErrorResponse(
                409,
                "CONFLICT",
                "Конфлікт даних. Можливо, даний запис вже існує",
                Instant.now()
        );
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
    }

    @ExceptionHandler(DataAccessException.class)
    public ResponseEntity<?> dataAccessException(DataAccessException e) {
        log.error("Критична помилка бази даних: {}", e.getMessage(), e);
        ErrorResponse errorResponse = new ErrorResponse(
                500,
                "INTERNAL_SERVER_ERROR",
                "Помилка сервера при роботі з даними",
                Instant.now()
        );
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
    }
}
