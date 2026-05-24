package com.project.ArtRari.auction.dto;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.time.Instant;

public record AuctionCreateRequest(
        @NotNull(message = "Для створення аукціону необхідне ID виставки")
        Long exhibitionId,
        @NotNull(message = "Для створення аукціону необхідне дата початку")
        @FutureOrPresent(message = "Дата початку аукціону має бути реальною")
        Instant startDate,
        @NotNull(message = "Для створення аукціону потрібен крок")
        @Positive(message = "Крок аукціону має бути більше 0")
        BigDecimal step,
        @NotNull(message = "Для створення аукціону необхідне дата кінця")
        @FutureOrPresent(message = "Дата кінця аукціону має бути реальною")
        Instant endDate
) {}
