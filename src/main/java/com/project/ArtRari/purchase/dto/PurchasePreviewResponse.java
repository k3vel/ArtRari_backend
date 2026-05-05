package com.project.ArtRari.purchase.dto;

import java.math.BigDecimal;

public record PurchasePreviewResponse(
        Long id,
        String title,
        String thumbnailUrl,
        BigDecimal finalPrice,
        String status
) {}
