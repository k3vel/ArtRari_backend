package com.project.ArtRari.auction;

import com.project.ArtRari.auction.dto.*;
import jakarta.websocket.server.PathParam;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/auctions")
@RequiredArgsConstructor
public class AuctionController {
    private final AuctionService auctionService;

    @GetMapping
    public AuctionsPageResponse getAuctions(@RequestParam(required = false) List<String> tags) {
        return auctionService.getAuctions(tags);
    }

    @GetMapping("/{id}")
    public AuctionResponse getAuction(@PathVariable Long id) {
        return auctionService.getById(id);
    }

    @PostMapping
    @PreAuthorize("hasRole('curator')")
    public AuctionResponse createAuction(@RequestBody AuctionCreateRequest request) {
        return auctionService.createAuction(request);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('curator')")
    public AuctionResponse updateAuction(@PathVariable Long id, @RequestBody AuctionUpdateRequest request) {
        return auctionService.updateAuction(id, request);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('curator')")
    public void deleteAuction(@PathVariable Long id) {
        auctionService.deleteAuction(id);
    }

    @GetMapping("/my")
    @PreAuthorize("hasRole('curator')")
    public List<AuctionPreviewResponse> getMyAuctions() {
        return auctionService.getMyAuctions();
    }

}
