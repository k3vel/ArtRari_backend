package com.project.ArtRari.exhibition;

import com.project.ArtRari.artwork.Artwork;
import com.project.ArtRari.artwork.ArtworkMapper;
import com.project.ArtRari.artwork.dto.ArtworkPreviewResponse;
import com.project.ArtRari.exhibition.dto.ExhibitionPreviewResponse;
import com.project.ArtRari.exhibition.dto.ExhibitionResponse;
import com.project.ArtRari.user.dto.UserPreviewResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class ExhibitionMapper {
    private final ArtworkMapper artworkMapper;

    public ExhibitionPreviewResponse mapExhibitionIntoExhibitionPreviewResponse(Exhibition exhibition) {
        return new ExhibitionPreviewResponse(
                exhibition.getId(),
                exhibition.getTitle(),
                exhibition.getTheme(),
                exhibition.getThumbnailUrl());
    }

    public ExhibitionResponse mapExhibitionIntoExhibitionResponse(Exhibition exhibition) {
        List<Artwork> artworks = exhibition.getArtworks();
        List<ArtworkPreviewResponse> safeArtworks = artworks.stream().map(a -> artworkMapper.toArtworkPreviewResponse(a)).toList();
        return new ExhibitionResponse(
                exhibition.getId(),
                exhibition.getTitle(),
                safeArtworks,
                exhibition.getDescription(),
                exhibition.getBackgroundUrl(),
                new UserPreviewResponse(
                        exhibition.getCurator().getId(),
                        exhibition.getCurator().getFirstName(),
                        exhibition.getCurator().getLastName()),
                exhibition.getStartDate(),
                exhibition.getStatus().name()
        );
    }
}
