package com.project.ArtRari.auction.dto;

import java.util.List;

public record AuctionsPageResponse(
        List<AuctionPreviewResponse> auctionPreviews
) {}
