package com.project.ArtRari.auth;

import jakarta.validation.constraints.NotBlank;

public record SignupRequest(
        @NotBlank(message = "Поле Ім'я є обов'язковим")
        String firstName,
        @NotBlank(message = "Поле Прізвище є обов'язковим")
        String lastName,
        @NotBlank(message = "Поле Телефон є обов'язковим")
        String phone,
        @NotBlank(message = "Поле Пошта є обов'язковим")
        String email,
        @NotBlank(message = "Поле Пароль є обов'язковим")
        String password
) {}
