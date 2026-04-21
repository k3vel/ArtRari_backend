package com.project.ArtRari.auction;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface AuctionRepository extends JpaRepository<Auction, Long> {
    @EntityGraph(attributePaths = {"exhibition"})
    List<Auction> findByEndDateAfterOrderByStartDateDesc(Instant now);

    @EntityGraph(attributePaths = {"exhibition"})
    List<Auction> findByStatusOrStatus(AuctionStatus status1, AuctionStatus status2);

    @Override
    @EntityGraph(attributePaths = {"lots", "lots.artwork", "lots.artwork.user", "exhibition"})
    Optional<Auction> findById(Long id);
}
