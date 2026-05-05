package com.project.ArtRari.lot;

import com.project.ArtRari.artwork.Artwork;
import com.project.ArtRari.artwork.ArtworkMapper;
import com.project.ArtRari.artwork.dto.ArtworkPreviewResponse;
import com.project.ArtRari.artwork.dto.ArtworkResponse;
import com.project.ArtRari.lot.dto.LotPreviewResponse;
import com.project.ArtRari.lot.dto.LotResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class LotMapper {
    private final ArtworkMapper artworkMapper;

    public LotResponse toLotResponse(Lot lot, Long userId) {
        Artwork artwork = lot.getArtwork();
        boolean isMyLot = artwork.getOwner().getId().equals(userId);
        ArtworkResponse safeArtwork = artworkMapper.toArtworkResponse(artwork);
        return new LotResponse(
                lot.getId(),
                safeArtwork,
                lot.getCurrentPrice(),
                lot.getEndDate(),
                lot.getStatus().name(),
                isMyLot
        );
    }

    public LotPreviewResponse toLotPreviewResponse(Lot lot) {
        Artwork artwork = lot.getArtwork();
        ArtworkPreviewResponse safeArtwork = artworkMapper.toArtworkPreviewResponse(artwork);
        return new LotPreviewResponse(
                lot.getId(),
                safeArtwork.title(),
                lot.getCurrentPrice(),
                lot.getEndDate(),
                safeArtwork.tags(),
                safeArtwork.thumbnailUrl()
        );
    }
}
