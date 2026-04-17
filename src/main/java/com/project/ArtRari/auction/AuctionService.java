package com.project.ArtRari.auction;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@AllArgsConstructor
public class AuctionService {
    private AuctionRepository auctionRepository;

    public List<Auction> getAllAuctions() {
        return auctionRepository.findAll();
    }

    public List<Auction> getLatestAuctions() {
        return auctionRepository.findByEndTypeAfterOrderByStartTimeDesc(LocalDateTime.now());
    }
}
