package com.project.ArtRari.homepage;

import com.project.ArtRari.auction.dto.AuctionPreviewResponse;
import com.project.ArtRari.exhibition.dto.ExhibitionPreviewResponse;

import java.util.List;

public record HomePageResponse(
        List<AuctionPreviewResponse> currentAuctions,
        List<ExhibitionPreviewResponse> topExhibitions
) {}
