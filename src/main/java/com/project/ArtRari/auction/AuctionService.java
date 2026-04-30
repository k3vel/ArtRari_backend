package com.project.ArtRari.auction;

import com.project.ArtRari.artwork.Artwork;
import com.project.ArtRari.auction.dto.*;
import com.project.ArtRari.exception.ArtrariException;
import com.project.ArtRari.exhibition.Exhibition;
import com.project.ArtRari.exhibition.ExhibitionRepository;
import com.project.ArtRari.exhibition.ExhibitionService;
import com.project.ArtRari.exhibition.ExhibitionStatus;
import com.project.ArtRari.lot.Lot;
import com.project.ArtRari.lot.LotMapper;
import com.project.ArtRari.lot.LotRepository;
import com.project.ArtRari.lot.LotStatus;
import com.project.ArtRari.lot.dto.LotPreviewResponse;
import com.project.ArtRari.lot.dto.LotResponse;
import com.project.ArtRari.security.UserDetailsImpl;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AuctionService {
    private final AuctionRepository auctionRepository;
    private final LotMapper lotMapper;
    private final ExhibitionRepository exhibitionRepository;
    private final LotRepository lotRepository;

    private List<AuctionPreviewResponse> mapAuctionsIntoAuctionPreviewResponse(List<Auction> auctions) {
        return auctions.stream()
                .map(a -> new AuctionPreviewResponse(
                        a.getId(),
                        a.getExhibition().getTitle(),
                        a.getExhibition().getTheme(),
                        a.getExhibition().getThumbnailUrl(),
                        a.getStartDate(),
                        a.getEndDate()))
                .toList();
    }

    private AuctionResponse mapAuctionIntoAuctionResponse(Auction auction) {
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
        return mapAuctionIntoAuctionResponse(auction);
    }

    @Transactional
    public AuctionResponse createAuction(AuctionCreateRequest request) {
        Exhibition exhibition = exhibitionRepository.findByIdAndStatus(request.exhibitionId(), ExhibitionStatus.running)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        if (auctionRepository.existsByExhibitionId(exhibition.getId())) {
            throw new ArtrariException(HttpStatus.CONFLICT, "Auction for this exhibition already exists");
        }
        UserDetailsImpl udi = (UserDetailsImpl) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (!udi.getId().equals(exhibition.getCurator().getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }

        Auction auction = new Auction();
        auction.setExhibition(exhibition);
        auction.setStartDate(request.startDate());
        auction.setEndDate(request.endDate());
        auction.setStep(request.step());
        auction.setStatus(AuctionStatus.scheduled);

        List<Lot> lots = new ArrayList<>();
        List<Artwork> artworks = exhibition.getArtworks();
        for (Artwork artwork : artworks) {
            Lot lot = new Lot();
            lot.setArtwork(artwork);
            lot.setCurrentPrice(artwork.getStartPrice());
            lot.setEndDate(request.endDate());
            lot.setStatus(LotStatus.available);
            lots.add(lot);
        }

        auction.setLots(lots);
        auctionRepository.save(auction);

        return mapAuctionIntoAuctionResponse(auction);
    }

    @Transactional
    public AuctionResponse updateAuction(Long id, AuctionUpdateRequest request) {
        Auction auction = auctionRepository.findById(id).orElseThrow(
                () -> new ResponseStatusException(HttpStatus.NOT_FOUND)
        );
        Exhibition exhibition = auction.getExhibition();
        UserDetailsImpl udi = (UserDetailsImpl) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (!udi.getId().equals(exhibition.getCurator().getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }
        if (auction.getStatus() == AuctionStatus.active || auction.getStatus() == AuctionStatus.finished) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Ви не можете редагувати аукціон, який проходить або завершений");
        }
        auction.setStartDate(request.startDate());
        auction.setEndDate(request.endDate());
        auction.setStep(request.step());
        auctionRepository.save(auction);
        return mapAuctionIntoAuctionResponse(auction);
    }

    @Transactional
    public void deleteAuction(Long id) {
        Auction auction = auctionRepository.findById(id).orElseThrow(
                () -> new ResponseStatusException(HttpStatus.NOT_FOUND)
        );
        Exhibition exhibition = auction.getExhibition();
        UserDetailsImpl udi = (UserDetailsImpl) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (!udi.getId().equals(exhibition.getCurator().getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }
        if (auction.getStatus() == AuctionStatus.active || auction.getStatus() == AuctionStatus.finished) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Ви не можете видаляти аукціон, який проходить або завершений");
        }
        auctionRepository.delete(auction);
    }

    @Transactional
    public void openAuctions() {
        List<Auction> auctions = auctionRepository.findAuctionsToOpen(Instant.now());
        for (Auction auction : auctions) {
            auction.setStatus(AuctionStatus.active);
            Exhibition e = auction.getExhibition();
            e.setStatus(ExhibitionStatus.converted_into_auction);
        }
    }

    @Transactional
    public void closeAuctions() {
        List<Auction> auctions = auctionRepository.findAuctionsToClose(Instant.now());
        for (Auction auction : auctions) {
            auction.setStatus(AuctionStatus.finished);
        }
    }

}
