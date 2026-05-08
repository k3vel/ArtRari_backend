package com.project.ArtRari.user.dto;

public record ProfileUpdateRequest(
        String newFirstName,
        String newLastName,
        String newPhone,
        String newEmail
) {}
