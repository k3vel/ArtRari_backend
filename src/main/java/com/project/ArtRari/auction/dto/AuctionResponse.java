package com.project.ArtRari.auction.dto;

import com.project.ArtRari.lot.Lot;
import com.project.ArtRari.lot.dto.LotPreviewResponse;
import com.project.ArtRari.lot.dto.LotResponse;

import java.time.Instant;
import java.util.List;

public record AuctionResponse(
        Long id,
        List<LotPreviewResponse> lotPreviews,
        Instant startDate,
        Instant endDate,
        String status
) {}
