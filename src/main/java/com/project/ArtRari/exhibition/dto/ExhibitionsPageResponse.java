package com.project.ArtRari.exhibition.dto;

import java.util.List;

public record ExhibitionsPageResponse(
        List<String> tags,
        List<ExhibitionPreviewResponse> exhibitionPreviews
) {}
