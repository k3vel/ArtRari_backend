package com.project.ArtRari.auction.dto;

import com.project.ArtRari.lot.dto.LotPreviewResponse;
import com.project.ArtRari.user.dto.UserPreviewResponse;

import java.time.Instant;
import java.util.List;

public record AuctionResponse(
        Long id,
        String theme,
        String description,
        List<LotPreviewResponse> lotPreviews,
        Instant startDate,
        Instant endDate,
        UserPreviewResponse curator,
        String status
) {}
