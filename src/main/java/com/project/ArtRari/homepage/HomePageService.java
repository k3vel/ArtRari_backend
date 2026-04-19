package com.project.ArtRari.homepage;

import com.project.ArtRari.auction.dto.AuctionPreviewResponse;
import com.project.ArtRari.auction.AuctionService;
import com.project.ArtRari.exhibition.dto.ExhibitionPreviewResponse;
import com.project.ArtRari.exhibition.ExhibitionService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@AllArgsConstructor
public class HomePageService {
    private AuctionService auctionService;
    private ExhibitionService exhibitionService;

    public HomePageResponse getHomePage() {
        List<AuctionPreviewResponse> auctions = auctionService.getLatestAuctionsPreview();
        List<ExhibitionPreviewResponse> exhibitions = exhibitionService.getTop6Preview();
        return new HomePageResponse(auctions, exhibitions);
    }
}
