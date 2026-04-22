package com.project.ArtRari.artwork.dto;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

public record ArtworkUpdateRequest(
        //Long userId,
        //Long artworkId,
        String newTitle,
        String newAuthor,
        String newDescription,
        String newTechnique,
        List<Long> newTags,
        Date newCreationDate,
        String newPhotoUrl,
        BigDecimal newStartPrice
) {}
