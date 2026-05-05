package com.project.ArtRari.auction;

import com.project.ArtRari.artwork.Artwork;
import com.project.ArtRari.artwork.tag.TagRepository;
import com.project.ArtRari.auction.dto.*;
import com.project.ArtRari.exception.ArtrariException;
import com.project.ArtRari.exhibition.Exhibition;
import com.project.ArtRari.exhibition.ExhibitionRepository;
import com.project.ArtRari.exhibition.ExhibitionStatus;
import com.project.ArtRari.lot.Lot;
import com.project.ArtRari.lot.LotMapper;
import com.project.ArtRari.lot.LotStatus;
import com.project.ArtRari.security.UserDetailsImpl;
import jakarta.transaction.Transactional;
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
    private final AuctionMapper auctionMapper;
    private final ExhibitionRepository exhibitionRepository;
    private final TagRepository tagRepository;

    public List<AuctionPreviewResponse> getLatestAuctionsPreview() {
        List<Auction> auctions = auctionRepository.findByEndDateAfterOrderByStartDateDesc(Instant.now());
        return auctionMapper.mapAuctionsIntoAuctionPreviewResponse(auctions);
    }

    public AuctionsPageResponse getAuctions(List<String> selectedTags) {
        List<Auction> auctions;
        boolean tagsIsEmpty = selectedTags == null || selectedTags.isEmpty();
        if (tagsIsEmpty) {
            auctions = auctionRepository.findByStatusOrStatus(AuctionStatus.scheduled, AuctionStatus.active);
        } else {
            auctions = auctionRepository.findByTags(selectedTags);
        }
        List<AuctionPreviewResponse> aprs = auctionMapper.mapAuctionsIntoAuctionPreviewResponse(auctions);
        List<String> tags = tagRepository.findAll().stream().map(tag -> tag.getName()).toList();
        return new AuctionsPageResponse(tags, aprs);
    }

    public AuctionResponse getById(Long id) {
        Auction auction = auctionRepository.findById(id).orElseThrow(
                () -> new ResponseStatusException(HttpStatus.NOT_FOUND)
        );
        return auctionMapper.mapAuctionIntoAuctionResponse(auction);
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

        return auctionMapper.mapAuctionIntoAuctionResponse(auction);
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
        return auctionMapper.mapAuctionIntoAuctionResponse(auction);
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

    public List<AuctionPreviewResponse> getMyAuctions() {
        UserDetailsImpl udi = (UserDetailsImpl) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        List<Auction> auctions = auctionRepository.findByExhibitionCuratorId(udi.getId());
        return auctionMapper.mapAuctionsIntoAuctionPreviewResponse(auctions);
    }

}
