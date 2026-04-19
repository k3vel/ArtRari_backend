package com.project.ArtRari.exhibition.dto;

import com.project.ArtRari.exhibition.ExhibitionStatus;

import java.time.Instant;

public record ExhibitionResponse(
   Long id,
   String title,
   String description,
   String backgroundUrl,
   Instant startDate,
   ExhibitionStatus status
) {}
