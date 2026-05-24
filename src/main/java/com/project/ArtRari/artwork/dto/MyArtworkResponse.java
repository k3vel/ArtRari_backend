package com.project.ArtRari.artwork.dto;

import com.project.ArtRari.artwork.tag.TagResponse;
import com.project.ArtRari.user.dto.UserPreviewResponse;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record MyArtworkResponse(
        Long id,
        UserPreviewResponse owner,
        String title,
        String author,
        String description,
        String technique,
        List<TagResponse> tags,
        LocalDate creationDate,
        String photoUrl,
        BigDecimal startPrice,
        String status
) {}
