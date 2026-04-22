package com.project.ArtRari.exhibition.dto;

import com.project.ArtRari.artwork.dto.ArtworkPreviewResponse;
import com.project.ArtRari.user.dto.UserPreviewResponse;

import java.time.Instant;
import java.util.List;

public record ExhibitionResponse(
        Long id,
        String title,
        List<ArtworkPreviewResponse> artworks,
        String description,
        String backgroundUrl,
        UserPreviewResponse curator,
        Instant startDate,
        String status
) {}
