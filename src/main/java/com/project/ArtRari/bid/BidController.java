package com.project.ArtRari.bid;

import com.project.ArtRari.bid.dto.BidPlaceRequest;
import com.project.ArtRari.bid.dto.BidPreviewResponse;
import com.project.ArtRari.security.UserDetailsImpl;
import lombok.RequiredArgsConstructor;
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
    public List<BidPreviewResponse> getBids(
            @PathVariable Long lotId,
            @AuthenticationPrincipal UserDetailsImpl udi
    ) {
        return bidService.getBidPreviews(lotId, udi);
    }

    @PostMapping
    @PreAuthorize("hasRole('user')")
    public void placeBid(
            @PathVariable Long lotId,
            @RequestBody BidPlaceRequest bidPlaceRequest,
            @AuthenticationPrincipal UserDetailsImpl udi
    ) {
        bidService.placeBid(lotId, bidPlaceRequest.amount(), udi);
    }

}
