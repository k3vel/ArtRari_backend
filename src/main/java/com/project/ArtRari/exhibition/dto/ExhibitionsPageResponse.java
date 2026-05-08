package com.project.ArtRari.exhibition.dto;

import com.project.ArtRari.common.PageResponse;

import java.util.List;

public record ExhibitionsPageResponse(
    List<String> tagsForFilter,
    PageResponse<ExhibitionPreviewResponse> page
) {}
