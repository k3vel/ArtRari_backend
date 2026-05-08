package com.project.ArtRari.user.dto;

public record ProfileResponse(
        Long id,
        String firstName,
        String lastName,
        String phone,
        String email
) {}
