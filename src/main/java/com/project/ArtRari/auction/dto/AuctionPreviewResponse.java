package com.project.ArtRari.auction.dto;

import com.project.ArtRari.auction.AuctionStatus;

import java.time.Instant;

public record AuctionPreviewResponse(
        Long id,
        String title,
        String theme,
        String description,
        String thumbnailUrl,
        AuctionStatus status,
        Instant startDate,
        Instant endDate
) {}

