package com.project.ArtRari.purchase.dto;

import java.math.BigDecimal;

public record PurchasePreviewResponse(
        Long lotId,
        String title,
        String thumbnailUrl,
        BigDecimal finalPrice,
        String status
) {}
