package com.project.ArtRari.artwork.dto;

import java.math.BigDecimal;
import java.util.List;

public record ArtworkAdvancedPreviewResponse(
        Long id,
        String title,
        String author,
        List<String> tags,
        String technique,
        BigDecimal startPrice,
        String thumbnailUrl
) {}
