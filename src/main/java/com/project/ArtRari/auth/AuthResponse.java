package com.project.ArtRari.auth;

public record AuthResponse(
        String jwtToken,
        Long userId,
        String firstName,
        String lastName,
        String role
) {}
