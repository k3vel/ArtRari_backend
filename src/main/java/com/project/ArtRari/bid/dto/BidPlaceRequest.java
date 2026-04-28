package com.project.ArtRari.bid.dto;

import java.math.BigDecimal;

public record BidPlaceRequest(
        BigDecimal amount
) {}
