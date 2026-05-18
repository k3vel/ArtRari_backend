package com.project.ArtRari.bid.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

public record BidPlaceRequest(
        @NotNull(message = "Ставка не можу бути порожня")
        @Positive(message = "Ставка повинна бути більше 0")
        BigDecimal amount
) {}
