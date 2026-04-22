package com.project.ArtRari.auction;

import com.project.ArtRari.auction.dto.AuctionPreviewResponse;
import com.project.ArtRari.auction.dto.AuctionResponse;
import com.project.ArtRari.auction.dto.AuctionsPageResponse;
import com.project.ArtRari.lot.Lot;
import com.project.ArtRari.lot.LotMapper;
import com.project.ArtRari.lot.dto.LotPreviewResponse;
import com.project.ArtRari.lot.dto.LotResponse;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AuctionService {
    private final AuctionRepository auctionRepository;
    private final LotMapper lotMapper;

    private List<AuctionPreviewResponse> mapAuctionsIntoAuctionPreviewResponse(List<Auction> auctions) {
        return auctions.stream()
                .map(a -> new AuctionPreviewResponse(
                        a.getId(),
                        a.getExhibition().getTitle(),
                        a.getExhibition().getTheme(),
                        //a.getExhibition().getDescription(),
                        a.getExhibition().getThumbnailUrl(),
                        //a.getStatus(),
                        a.getStartDate(),
                        a.getEndDate()))
                .toList();
    }

    public List<AuctionPreviewResponse> getLatestAuctionsPreview() {
        List<Auction> auctions = auctionRepository.findByEndDateAfterOrderByStartDateDesc(Instant.now());
        return mapAuctionsIntoAuctionPreviewResponse(auctions);
    }

    public AuctionsPageResponse getAuctions() {
        List<Auction> auctions = auctionRepository.findByStatusOrStatus(AuctionStatus.scheduled, AuctionStatus.active);
        return new AuctionsPageResponse(mapAuctionsIntoAuctionPreviewResponse(auctions));
    }

    public AuctionResponse getById(Long id) {
        Auction auction = auctionRepository.findById(id).orElseThrow(
                () -> new ResponseStatusException(HttpStatus.NOT_FOUND)
        );
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
