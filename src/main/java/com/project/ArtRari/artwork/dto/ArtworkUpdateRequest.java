package com.project.ArtRari.artwork.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Date;
import java.util.List;

public record ArtworkUpdateRequest(
        String newTitle,
        String newAuthor,
        String newDescription,
        String newTechnique,
        List<Long> newTags,
        LocalDate newCreationDate,
        String newPhotoUrl,
        BigDecimal newStartPrice
) {}
