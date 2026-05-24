package com.project.ArtRari.exhibition.dto;

public record ExhibitionPreviewResponse(
        Long id,
        String title,
        String theme,
        String thumbnailUrl,
        String status
) {}
