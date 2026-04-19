package com.project.ArtRari.auction;

import com.project.ArtRari.auction.dto.AuctionPreviewResponse;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

@Service
@AllArgsConstructor
public class AuctionService {
    private AuctionRepository auctionRepository;
/*
    public List<Auction> getAllAuctions() {
        return auctionRepository.findAll();
    }
*/
    public List<AuctionPreviewResponse> getLatestAuctionsPreview() {
        List<Auction> auctions = auctionRepository.findByEndDateAfterOrderByStartDateDesc(Instant.now());
        return auctions.stream()
                .map(a -> new AuctionPreviewResponse(
                        a.getId(),
                        a.getExhibition().getTitle(),
                        a.getExhibition().getTheme(),
                        a.getExhibition().getDescription(),
                        a.getExhibition().getThumbnailUrl(),
                        a.getStatus(),
                        a.getStartDate(),
                        a.getEndDate()))
                .toList();
    }
}
