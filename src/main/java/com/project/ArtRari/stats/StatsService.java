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
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class StatsService {
    private final ArtworkRepository artworkRepository;
    private final ExhibitionRepository exhibitionRepository;
    private final AuctionRepository auctionRepository;
    private final BidRepository bidRepository;

    public StatsResponse getStats(UserDetailsImpl udi) {
        if (udi.getRole().equals(Role.user)) {
            int bids = bidRepository.countAllByUserId(udi.getId());
            int wins = bidRepository.countAllByIsWinTrueAndUserId(udi.getId());
            int artworks = artworkRepository.countAllByOwnerId(udi.getId());
            return new UserStatsResponse(bids, wins, artworks);
        } else if (udi.getRole().equals(Role.curator)) {
            int exhibitions = exhibitionRepository.countAllByCuratorId(udi.getId());
            int auctions = auctionRepository.countAllByExhibitionCuratorId(udi.getId());
            return new CuratorStatsResponse(exhibitions, auctions);
        } else return null; //todo for admin
    }
}
