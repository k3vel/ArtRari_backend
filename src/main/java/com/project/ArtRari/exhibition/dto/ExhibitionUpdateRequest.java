package com.project.ArtRari.exhibition.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record ExhibitionUpdateRequest(
        @NotNull(message = "Поле Роботи є обов'язковим")
        List<Long> artworkIds,
        @NotBlank(message = "Виставка повинна мати назву")
        String title,
        @NotBlank(message = "Виставка повинная мати тематику")
        String theme,
        @NotNull(message = "Поле опис є обов'язковим")
        String description,
        @NotNull(message = "Поле Фон є обов'язковим")
        String backgroundUrl,
        @NotNull(message = "Поле Іконка є обов'язковим")
        String thumbnailUrl
) {}
