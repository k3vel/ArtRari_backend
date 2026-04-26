package com.project.ArtRari.exhibition.dto;

import java.util.List;

public record ExhibitionUpdateRequest(
        List<Long> artworkIds,
        String title,
        String theme,
        String description,
        String backgroundUrl,
        String thumbnailUrl
) {}
