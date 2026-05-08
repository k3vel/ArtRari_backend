package com.project.ArtRari.auction;

import com.project.ArtRari.auction.dto.AuctionPreviewResponse;
import com.project.ArtRari.auction.dto.AuctionResponse;
import com.project.ArtRari.lot.Lot;
import com.project.ArtRari.lot.LotMapper;
import com.project.ArtRari.lot.dto.LotPreviewResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class AuctionMapper {
    private final LotMapper lotMapper;

    public AuctionPreviewResponse mapAuctionIntoAuctionPreviewResponse(Auction auction) {
        return new AuctionPreviewResponse(
                auction.getId(),
                auction.getExhibition().getTitle(),
                auction.getExhibition().getTheme(),
                auction.getExhibition().getThumbnailUrl(),
                auction.getStatus(),
                auction.getStartDate(),
                auction.getEndDate());
    }

    public AuctionResponse mapAuctionIntoAuctionResponse(Auction auction) {
        List<Lot> lots = auction.getLots();
        List<LotPreviewResponse> safeLots = lots.stream().map(l -> lotMapper.toLotPreviewResponse(l)).toList();
        return new AuctionResponse(
                auction.getId(),
                safeLots,
                auction.getStartDate(),
                auction.getEndDate(),
                auction.getStatus().name()
        );
    }
}
