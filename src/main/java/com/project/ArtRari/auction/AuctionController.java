package com.project.ArtRari.auction;

import com.project.ArtRari.auction.dto.AuctionPreviewResponse;
import com.project.ArtRari.auction.dto.AuctionResponse;
import com.project.ArtRari.auction.dto.AuctionsPageResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/auctions")
@RequiredArgsConstructor
public class AuctionController {
    private final AuctionService auctionService;

    @GetMapping
    public AuctionsPageResponse getAuctions() {
        return auctionService.getAuctions();
    }

    @GetMapping("/{id}")
    public AuctionResponse getAuction(@PathVariable Long id) {
        return auctionService.getById(id);
    }
}
