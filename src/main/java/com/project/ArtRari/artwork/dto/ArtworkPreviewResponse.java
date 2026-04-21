package com.project.ArtRari.artwork.dto;

import java.util.List;

public record ArtworkPreviewResponse(
        Long id,
        String title,
        List<String> tags,
        String thumbnailUrl
) {}
