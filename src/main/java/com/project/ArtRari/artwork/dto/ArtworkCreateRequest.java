package com.project.ArtRari.artwork.dto;

import com.project.ArtRari.user.User;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

public record ArtworkCreateRequest(
        Long userId,
        String title,
        String author,
        String description,
        String technique,
        List<Long> tags,
        Date creationDate,
        String photoUrl,
        BigDecimal startPrice
) {}
