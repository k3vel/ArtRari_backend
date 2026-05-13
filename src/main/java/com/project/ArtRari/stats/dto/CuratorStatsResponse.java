package com.project.ArtRari.stats.dto;

public record CuratorStatsResponse(
        int exhibitions,
        int auctions
) implements StatsResponse{}
