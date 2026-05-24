package com.project.ArtRari.lot.dto;

public record LotWonEvent(
        String winnerEmail,
        LotPreviewResponse lot
) {}
