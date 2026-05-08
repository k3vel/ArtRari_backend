package com.project.ArtRari.auction.dto;

import com.project.ArtRari.common.PageResponse;

import java.util.List;

public record AuctionsPageResponse(
        List<String> tags,
        PageResponse<AuctionPreviewResponse> page
) {}
