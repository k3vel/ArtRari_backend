package com.project.ArtRari.artwork.dto;

import jakarta.validation.constraints.*;
import org.hibernate.validator.constraints.URL;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record ArtworkUpdateRequest(
        @NotBlank(message = "Поле Назва є обов'язковим")
        String newTitle,
        @NotBlank(message = "Поле Автор є обов'язковим")
        String newAuthor,
        @NotNull(message = "Поле Опис є обов'язковим")
        String newDescription,
        @NotNull(message = "Поле Техника є обов'язковим")
        String newTechnique,
        @NotNull(message = "Поле Теги є обов'язковим")
        List<Long> newTags,
        @NotNull(message = "Поле Дата створення є обов'язковим")
        @PastOrPresent(message = "Дата створення має бути реальною")
        LocalDate newCreationDate,
        @NotBlank(message = "Поле Зображення є обов'язковим")
        @URL(message = "Невірний формат посилання")
        String newPhotoUrl,
        @NotNull(message = "Поле Ціна є обов'язковим")
        @PositiveOrZero(message = "Стартова ціна має бути не менше 0")
        BigDecimal newStartPrice
) {}
