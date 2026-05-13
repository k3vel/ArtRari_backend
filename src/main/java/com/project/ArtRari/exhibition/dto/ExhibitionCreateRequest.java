package com.project.ArtRari.exhibition.dto;

import java.util.List;

public record ExhibitionCreateRequest(
        List<Long> artworkIds,
        String title,
        String theme,
        String description,
        String backgroundUrl,
        String thumbnailUrl
) {}
