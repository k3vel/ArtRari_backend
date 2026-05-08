package com.project.ArtRari.user.dto;

public record PasswordChangeRequest(
        String oldPassword,
        String newPassword
) {}
