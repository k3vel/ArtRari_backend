package com.project.ArtRari.auction;

import com.project.ArtRari.auction.dto.*;
import com.project.ArtRari.common.PageResponse;
import com.project.ArtRari.security.UserDetailsImpl;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/auctions")
@RequiredArgsConstructor
public class AuctionController {
    private final AuctionService auctionService;

    @GetMapping
    public ResponseEntity<AuctionsPageResponse> getAuctions(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(required = false) List<String> tags
    ) {
        return ResponseEntity.ok(auctionService.getAuctions(page, tags));
    }

    @GetMapping("/{id}")
    public ResponseEntity<AuctionResponse> getAuction(@PathVariable Long id) {
        return ResponseEntity.ok(auctionService.getById(id));
    }

    @PostMapping
    @PreAuthorize("hasRole('curator')")
    public ResponseEntity<AuctionResponse> createAuction(
            @Valid @RequestBody AuctionCreateRequest request,
            @AuthenticationPrincipal UserDetailsImpl udi) {
        AuctionResponse auctionResponse = auctionService.createAuction(request, udi);
        URI uri = URI.create("/auctions/" + auctionResponse.id());
        return ResponseEntity.created(uri).body(auctionResponse);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('curator')")
    public ResponseEntity<AuctionResponse> updateAuction(
            @PathVariable Long id,
            @Valid @RequestBody AuctionUpdateRequest request,
            @AuthenticationPrincipal UserDetailsImpl udi) {
        AuctionResponse auctionResponse = auctionService.updateAuction(id, request, udi);
        return ResponseEntity.ok(auctionResponse);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('curator')")
    public ResponseEntity<?> deleteAuction(@PathVariable Long id, @AuthenticationPrincipal UserDetailsImpl udi) {
        auctionService.deleteAuction(id, udi);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/my")
    @PreAuthorize("hasRole('curator')")
    public ResponseEntity<PageResponse<AuctionPreviewResponse>> getMyAuctions(
            @RequestParam(defaultValue = "0") int page,
            @AuthenticationPrincipal UserDetailsImpl udi
    ) {
        PageResponse<AuctionPreviewResponse> pageResponse = auctionService.getMyAuctions(page, udi);
        return ResponseEntity.ok(pageResponse);
    }

}
