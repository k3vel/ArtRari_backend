package com.project.ArtRari.auth;

import jakarta.validation.constraints.NotBlank;

public record SigninRequest(
        @NotBlank(message = "Поле Пошта є обов'язковим")
        String email,
        @NotBlank(message = "Поле Пароль є обов'язковим")
        String password
) {}
