package com.project.ArtRari.stats;

import com.project.ArtRari.artwork.ArtworkRepository;
import com.project.ArtRari.artwork.WorkStatus;
import com.project.ArtRari.auction.AuctionRepository;
import com.project.ArtRari.auction.AuctionStatus;
import com.project.ArtRari.bid.BidRepository;
import com.project.ArtRari.exhibition.ExhibitionRepository;
import com.project.ArtRari.exhibition.ExhibitionStatus;
import com.project.ArtRari.purchase.PurchaseRepository;
import com.project.ArtRari.security.UserDetailsImpl;
import com.project.ArtRari.stats.dto.AdminStatsResponse;
import com.project.ArtRari.stats.dto.CuratorStatsResponse;
import com.project.ArtRari.stats.dto.StatsResponse;
import com.project.ArtRari.stats.dto.UserStatsResponse;
import com.project.ArtRari.user.Role;
import com.project.ArtRari.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

@Service
@RequiredArgsConstructor
public class StatsService {
    private final ArtworkRepository artworkRepository;
    private final ExhibitionRepository exhibitionRepository;
    private final AuctionRepository auctionRepository;
    private final BidRepository bidRepository;
    private final UserRepository userRepository;
    private final PurchaseRepository purchaseRepository;

    @Cacheable(value = "stats", key = "#udi.id")
    public StatsResponse getStats(UserDetailsImpl udi) {
        return switch (udi.getRole()) {
            case Role.user -> {
                int bids = bidRepository.countAllByUserId(udi.getId());
                int wins = bidRepository.countAllByIsWinTrueAndUserId(udi.getId());
                int artworks = artworkRepository.countAllByOwnerId(udi.getId());
                yield new UserStatsResponse(bids, wins, artworks);
            }
            case Role.curator-> {
                int exhibitions = exhibitionRepository.countAllByCuratorId(udi.getId());
                int auctions = auctionRepository.countAllByExhibitionCuratorId(udi.getId());
                yield new CuratorStatsResponse(exhibitions, auctions);
            }
            case Role.admin -> {
                int exhibitions = exhibitionRepository.countAllByStatus(ExhibitionStatus.running);
                int auctions = auctionRepository.countAllByStatus(AuctionStatus.active);
                int artworks = artworkRepository.countAllByStatus(WorkStatus.available);
                long users = userRepository.count();
                Instant now = Instant.now();
                int bidsToday = bidRepository.countAllByCreatedAtBetween(now.minus(1, ChronoUnit.DAYS), now);
                int totalPriceLastWeek = purchaseRepository.totalPriceByWinDateBetween(now.minus(7, ChronoUnit.DAYS), now);
                yield new AdminStatsResponse(artworks, exhibitions, auctions, users, bidsToday, totalPriceLastWeek);
            }
        };
    }
}
