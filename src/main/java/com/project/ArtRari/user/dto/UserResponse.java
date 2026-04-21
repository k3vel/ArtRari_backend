package com.project.ArtRari.user.dto;

public record UserResponse(
        Long id,
        String fullName,
        String phone,
        String email
) {
}
