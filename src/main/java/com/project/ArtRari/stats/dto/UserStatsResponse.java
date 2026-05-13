package com.project.ArtRari.stats.dto;

public record UserStatsResponse(
        int bids,
        int wins,
        int artworks
) implements StatsResponse {}
