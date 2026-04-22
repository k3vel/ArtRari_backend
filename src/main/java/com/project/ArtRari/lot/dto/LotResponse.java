package com.project.ArtRari.lot.dto;

import com.project.ArtRari.artwork.dto.ArtworkResponse;

import java.math.BigDecimal;
import java.time.Instant;

public record LotResponse(
   Long id,
   ArtworkResponse artwork,
   BigDecimal currentPrice,
   Instant endDate,
   String status
) {} //todo список ставок
