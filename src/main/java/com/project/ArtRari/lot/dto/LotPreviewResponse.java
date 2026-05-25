package com.project.ArtRari.lot.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public record LotPreviewResponse(
        Long id,
        String title,
        BigDecimal currentPrice,
        Instant endDate,
        List<String> tags,
        String thumbnailUrl,
        String status
) {}
