package com.project.ArtRari.auction.dto;

import java.math.BigDecimal;
import java.time.Instant;

public record AuctionCreateRequest(
        Long exhibitionId,
        Instant startDate,
        BigDecimal step,
        Instant endDate
) {}
