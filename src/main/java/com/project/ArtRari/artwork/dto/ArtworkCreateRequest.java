package com.project.ArtRari.artwork.dto;

import com.project.ArtRari.user.User;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Date;
import java.util.List;

public record ArtworkCreateRequest(
        String title,
        String author,
        String description,
        String technique,
        List<Long> tags,
        LocalDate creationDate,
        String photoUrl,
        BigDecimal startPrice
) {}
