package com.project.ArtRari.auction.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.time.Instant;

public record AuctionUpdateRequest(
        @NotNull(message = "Аукціон повинен мати дату початку")
        @PastOrPresent(message = "Дата початку аукціону має бути реальною")
        Instant startDate,
        @NotNull(message = "Аукціон повинен мати дату кінця")
        @PastOrPresent(message = "Дата кінця аукціону має бути реальною")
        Instant endDate,
        @NotNull(message = "Аукціон повинен мати крок")
        @Positive(message = "Крок аукціону має бути більше 0")
        BigDecimal step
) {}
