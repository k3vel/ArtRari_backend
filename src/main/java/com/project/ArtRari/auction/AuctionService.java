package com.project.ArtRari.auction;

import com.project.ArtRari.artwork.Artwork;
import com.project.ArtRari.artwork.tag.TagRepository;
import com.project.ArtRari.auction.dto.*;
import com.project.ArtRari.common.PageResponse;
import com.project.ArtRari.exception.ArtrariException;
import com.project.ArtRari.exhibition.Exhibition;
import com.project.ArtRari.exhibition.ExhibitionRepository;
import com.project.ArtRari.exhibition.ExhibitionStatus;
import com.project.ArtRari.exhibition.dto.ExhibitionPreviewResponse;
import com.project.ArtRari.lot.Lot;
import com.project.ArtRari.lot.LotStatus;
import com.project.ArtRari.security.UserDetailsImpl;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AuctionService {
    private final AuctionRepository auctionRepository;
    private final AuctionMapper auctionMapper;
    private final ExhibitionRepository exhibitionRepository;
    private final TagRepository tagRepository;

    @Value("${app.pagination.default-size}")
    private int pageSize;

    public List<AuctionPreviewResponse> getLatestAuctionsPreview() {
        List<Auction> auctions = auctionRepository.findByEndDateAfterOrderByStartDateDesc(Instant.now());
        return auctions.stream().map(
                auctionMapper::mapAuctionIntoAuctionPreviewResponse
        ).collect(Collectors.toList());
    }

    public AuctionsPageResponse getAuctions(int page, List<String> selectedTags) {
        Page<Auction> auctions;
        Pageable pageable = PageRequest.of(page, pageSize, Sort.by("startDate").descending());
        boolean tagsIsEmpty = selectedTags == null || selectedTags.isEmpty();
        if (tagsIsEmpty) {
            auctions = auctionRepository.findByStatusOrStatus(AuctionStatus.scheduled, AuctionStatus.active, pageable);
        } else {
            auctions = auctionRepository.findByTags(selectedTags, pageable);
        }
        Page<AuctionPreviewResponse> apr = auctions.map(auctionMapper::mapAuctionIntoAuctionPreviewResponse);
        PageResponse<AuctionPreviewResponse> aprs = new PageResponse<>(apr);
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
        exhibition.setStatus(ExhibitionStatus.running);
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

    public PageResponse<AuctionPreviewResponse> getMyAuctions(int page) {
        UserDetailsImpl udi = (UserDetailsImpl) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Pageable pageable = PageRequest.of(page, pageSize, Sort.by("startDate").descending());
        Page<Auction> auctions = auctionRepository.findByExhibitionCuratorId(udi.getId(), pageable);
        Page<AuctionPreviewResponse> eprs = auctions.map(auctionMapper::mapAuctionIntoAuctionPreviewResponse);
        return new PageResponse<>(eprs);
    }

}
