package com.project.ArtRari.user.dto;

public record UserResponse(
        Long id,
        String firstName,
        String lastName,
        String phone,
        String email,
        String role
) {}
