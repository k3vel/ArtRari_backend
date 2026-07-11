package com.project.ArtRari.exhibition.dto;

import java.io.Serializable;

public record ExhibitionPreviewResponse(
        Long id,
        String title,
        String theme,
        String thumbnailUrl,
        String status
) implements Serializable {}
