package com.project.ArtRari.auction;

import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AuctionStateScheduler {
    private final AuctionService auctionService;

    @Scheduled(fixedRate = 60000)
    public void openAuctions() {
        auctionService.openAuctions();
    }

    @Scheduled(fixedRate = 10000)
    public void closeAuctions() {
        auctionService.closeAuctions();
    }

}
