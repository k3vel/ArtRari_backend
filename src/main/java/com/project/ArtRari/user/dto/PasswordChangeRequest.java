package com.project.ArtRari.user.dto;

import jakarta.validation.constraints.NotBlank;

public record PasswordChangeRequest(
        @NotBlank(message = "Поле Старий Пароль є обов'язковим")
        String oldPassword,
        @NotBlank(message = "Поле Новий Пароль є обов'язковим")
        String newPassword
) {}
