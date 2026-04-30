package com.project.ArtRari.exception;

import java.time.Instant;

public record ErrorResponse(
        int status,
        String value,
        String message,
        Instant timestamp
) {}
