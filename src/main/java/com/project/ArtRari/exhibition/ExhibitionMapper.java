package com.project.ArtRari.exhibition;

import com.project.ArtRari.artwork.Artwork;
import com.project.ArtRari.artwork.ArtworkMapper;
import com.project.ArtRari.artwork.dto.ArtworkAdvancedPreviewResponse;
import com.project.ArtRari.artwork.dto.ArtworkPreviewResponse;
import com.project.ArtRari.exhibition.dto.ExhibitionAdvancedResponse;
import com.project.ArtRari.exhibition.dto.ExhibitionPreviewResponse;
import com.project.ArtRari.exhibition.dto.ExhibitionResponse;
import com.project.ArtRari.user.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class ExhibitionMapper {
    private final ArtworkMapper artworkMapper;
    private final UserMapper userMapper;

    public ExhibitionPreviewResponse toExhibitionPreviewResponse(Exhibition exhibition) {
        return new ExhibitionPreviewResponse(
                exhibition.getId(),
                exhibition.getTitle(),
                exhibition.getTheme(),
                exhibition.getThumbnailUrl(),
                exhibition.getStatus().name()
        );
    }

    public ExhibitionResponse toExhibitionResponse(Exhibition exhibition) {
        List<Artwork> artworks = exhibition.getArtworks();
        List<ArtworkPreviewResponse> safeArtworks = artworks.stream().map(
                a -> artworkMapper.toArtworkPreviewResponse(a)
        ).toList();
        return new ExhibitionResponse(
                exhibition.getId(),
                exhibition.getTitle(),
                exhibition.getThumbnailUrl(),
                safeArtworks,
                exhibition.getDescription(),
                exhibition.getBackgroundUrl(),
                userMapper.toUserPreviewResponse(exhibition.getCurator()),
                exhibition.getStartDate(),
                exhibition.getStatus().name()
        );
    }

    public ExhibitionAdvancedResponse toExhibitionAdvancedResponse(Exhibition exhibition) {
        List<Artwork> artworks = exhibition.getArtworks();
        List<ArtworkAdvancedPreviewResponse> safeArtworks = artworks.stream().map(
                a -> artworkMapper.toArtworkAdvancedPreviewResponse(a)
        ).toList();
        return new ExhibitionAdvancedResponse(
                exhibition.getId(),
                exhibition.getTitle(),
                exhibition.getThumbnailUrl(),
                safeArtworks,
                exhibition.getDescription(),
                exhibition.getBackgroundUrl(),
                exhibition.getStartDate(),
                exhibition.getStatus().name()
        );
    }
}
