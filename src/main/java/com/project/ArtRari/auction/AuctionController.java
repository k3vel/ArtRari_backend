package com.project.ArtRari.auction;

import com.project.ArtRari.auction.dto.*;
import com.project.ArtRari.common.PageResponse;
import jakarta.websocket.server.PathParam;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/auctions")
@RequiredArgsConstructor
public class AuctionController {
    private final AuctionService auctionService;

    @GetMapping
    public AuctionsPageResponse getAuctions(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(required = false) List<String> tags
    ) {
        return auctionService.getAuctions(page, tags);
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
    public ResponseEntity<?> deleteAuction(@PathVariable Long id) {
        auctionService.deleteAuction(id);
        return ResponseEntity.ok("Аукціон успішно видалено");
    }

    @GetMapping("/my")
    @PreAuthorize("hasRole('curator')")
    public PageResponse<AuctionPreviewResponse> getMyAuctions(@RequestParam(defaultValue = "0") int page) {
        return auctionService.getMyAuctions(page);
    }

}
