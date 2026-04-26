package com.project.ArtRari.auction.dto;

import java.math.BigDecimal;
import java.time.Instant;

public record AuctionUpdateRequest(
        Instant startDate,
        Instant endDate,
        BigDecimal step
) {}
