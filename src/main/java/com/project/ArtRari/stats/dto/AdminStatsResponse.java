package com.project.ArtRari.stats.dto;

public record AdminStatsResponse(
        int activeArtworks,
        int activeExhibitions,
        int activeAuctions,
        long totalUsers,
        int bidsToday,
        int totalLotSoldPriceLastWeek
) implements StatsResponse {}
