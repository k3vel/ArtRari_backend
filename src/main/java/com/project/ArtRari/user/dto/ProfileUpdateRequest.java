package com.project.ArtRari.user.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record ProfileUpdateRequest(
        @NotBlank(message = "Поле Ім'я є обов'язковим")
        String newFirstName,
        @NotBlank(message = "Поле Прізвище є обов'язковим")
        String newLastName,
        @NotBlank(message = "Поле Телефон є обов'язковим")
        String newPhone,
        @Email(message = "Пошта має бути коректною")
        @NotBlank(message = "Поле Пошта є обов'язковим")
        String newEmail
) {}
