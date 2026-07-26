package com.project.ArtRari.auction;

import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AuctionStateScheduler {
    private final AuctionService auctionService;

    @Scheduled(fixedRateString = "${app.scheduler.auction-open-rate-ms:60000}")
    public void openAuctions() {
        auctionService.openAuctions();
    }

    @Scheduled(fixedRateString = "${app.scheduler.auction-close-rate-ms:10000}")
    public void closeAuctions() {
        auctionService.closeAuctions();
    }

}
