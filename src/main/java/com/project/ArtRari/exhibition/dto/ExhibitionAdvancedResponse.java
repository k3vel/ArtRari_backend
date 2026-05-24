package com.project.ArtRari.exhibition.dto;

import com.project.ArtRari.artwork.dto.ArtworkAdvancedPreviewResponse;

import java.time.Instant;
import java.util.List;

public record ExhibitionAdvancedResponse(
        Long id,
        String title,
        String thumbnailUrl,
        List<ArtworkAdvancedPreviewResponse> artworks,
        String description,
        String backgroundUrl,
        Instant startDate,
        String status
) {}
