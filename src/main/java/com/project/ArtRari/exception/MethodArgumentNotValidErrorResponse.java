package com.project.ArtRari.exception;

import java.time.Instant;
import java.util.Map;

public record MethodArgumentNotValidErrorResponse(
        int status,
        String value,
        Map<String, String> fieldErrors,
        Instant timestamp
) {}
