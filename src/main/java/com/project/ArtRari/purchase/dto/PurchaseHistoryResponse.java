package com.project.ArtRari.purchase.dto;

import java.util.List;

public record PurchaseHistoryResponse(
        List<PurchasePreviewResponse> purchases
) {}
