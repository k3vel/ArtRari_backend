package com.project.ArtRari.bid.dto;

public record BidPlacedEvent(
        Long lotId,
        BidPreviewResponse bidPreviewResponse
) {}
