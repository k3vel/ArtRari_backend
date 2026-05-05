package com.project.ArtRari.auction.dto;

import java.util.List;

public record AuctionsPageResponse(
        List<String> tags,
        List<AuctionPreviewResponse> auctionPreviews
) {}
