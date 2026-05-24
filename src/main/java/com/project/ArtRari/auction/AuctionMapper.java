package com.project.ArtRari.auction;

import com.project.ArtRari.auction.dto.AuctionPreviewResponse;
import com.project.ArtRari.auction.dto.AuctionResponse;
import com.project.ArtRari.lot.Lot;
import com.project.ArtRari.lot.LotMapper;
import com.project.ArtRari.lot.dto.LotPreviewResponse;
import com.project.ArtRari.user.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class AuctionMapper {
    private final LotMapper lotMapper;
    private final UserMapper userMapper;

    public AuctionPreviewResponse mapAuctionIntoAuctionPreviewResponse(Auction auction) {
        return new AuctionPreviewResponse(
                auction.getId(),
                auction.getExhibition().getTitle(),
                auction.getExhibition().getTheme(),
                auction.getExhibition().getDescription(),
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
                auction.getExhibition().getTheme(),
                auction.getExhibition().getDescription(),
                safeLots,
                auction.getStartDate(),
                auction.getEndDate(),
                userMapper.toUserPreviewResponse(auction.getExhibition().getCurator()),
                auction.getStatus().name()
        );
    }
}
