package com.project.ArtRari.bid;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BidRepository extends JpaRepository<Bid, Integer> {
    @EntityGraph(attributePaths = {"lot", "lot.auction"})
    Optional<Bid> findWithLotAndAuctionById(Long id);

    @EntityGraph(attributePaths = "user")
    List<Bid> findByLotIdOrderByCreatedAtDesc(Long lotId);

    @EntityGraph(attributePaths = {"user"})
    Optional<Bid> findTopByLotIdOrderByAmountDesc(Long lotId);

    int countAllByUserId(Long userId);

    int countAllByIsWinTrueAndUserId(Long userId);
}
