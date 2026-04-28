package com.project.ArtRari.bid.dto;

import java.math.BigDecimal;
import java.time.Instant;

public record BidPreviewResponse(
        String user,
        BigDecimal amount,
        Instant createdAt
) {}
