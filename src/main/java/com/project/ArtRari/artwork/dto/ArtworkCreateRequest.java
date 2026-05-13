package com.project.ArtRari.artwork.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.PositiveOrZero;
import org.hibernate.validator.constraints.URL;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record ArtworkCreateRequest(
        @NotBlank(message = "Назва роботи не може бути порожньою")
        String title,
        @NotBlank(message = "Робота має мати автора")
        String author,
        @NotNull(message = "Поле Опис є обов'язковим")
        String description,
        @NotNull(message = "Поле Техніка є обов'язковим")
        String technique,
        @NotNull(message = "Поле Теги є обов'язковим")
        List<Long> tags,
        @NotNull(message = "Робота має мати дату створення")
        @PastOrPresent(message = "Дата створення має бути реальною")
        LocalDate creationDate,
        @URL(message = "Невірний формат посилання")
        @NotBlank(message = "Робота має мати зображення")
        String photoUrl,
        @NotNull(message = "Робота має мати стартову ціну")
        @PositiveOrZero(message = "Стартова ціна має бути не менше 0")
        BigDecimal startPrice
) {}
