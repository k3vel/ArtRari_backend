package com.project.ArtRari.bid;

import com.project.ArtRari.bid.dto.BidPlaceRequest;
import com.project.ArtRari.bid.dto.BidPreviewResponse;
import com.project.ArtRari.security.UserDetailsImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/lots/{lotId}/bids")
@RequiredArgsConstructor
public class BidController {
    private final BidService bidService;

    @GetMapping
    public ResponseEntity<List<BidPreviewResponse>> getBids(
            @PathVariable Long lotId,
            @AuthenticationPrincipal UserDetailsImpl udi
    ) {
        return ResponseEntity.ok(bidService.getBidPreviews(lotId, udi));
    }

    @PostMapping
    @PreAuthorize("hasRole('user')")
    public ResponseEntity<BidPreviewResponse> placeBid(
            @PathVariable Long lotId,
            @RequestBody BidPlaceRequest bidPlaceRequest,
            @AuthenticationPrincipal UserDetailsImpl udi
    ) {
        BidPreviewResponse response = bidService.placeBid(lotId, bidPlaceRequest.amount(), udi);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

}
