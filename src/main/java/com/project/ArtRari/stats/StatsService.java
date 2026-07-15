package com.project.ArtRari.stats;

import com.project.ArtRari.artwork.ArtworkRepository;
import com.project.ArtRari.auction.AuctionRepository;
import com.project.ArtRari.bid.BidRepository;
import com.project.ArtRari.exhibition.ExhibitionRepository;
import com.project.ArtRari.security.UserDetailsImpl;
import com.project.ArtRari.stats.dto.CuratorStatsResponse;
import com.project.ArtRari.stats.dto.StatsResponse;
import com.project.ArtRari.stats.dto.UserStatsResponse;
import com.project.ArtRari.user.Role;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class StatsService {
    private final ArtworkRepository artworkRepository;
    private final ExhibitionRepository exhibitionRepository;
    private final AuctionRepository auctionRepository;
    private final BidRepository bidRepository;

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
                yield null; //todo продвинутые статы
            }
        };
    }
}
