package com.project.ArtRari.artwork.dto;

import com.project.ArtRari.user.dto.UserPreviewResponse;
import com.project.ArtRari.user.dto.UserResponse;

import java.util.Date;
import java.util.List;

public record ArtworkResponse(
        Long id,
        UserPreviewResponse owner,
        String title,
        String author,
        String description,
        String technique,
        List<String> tags,
        Date creationDate,
        String photoUrl,
        String status
) {}
