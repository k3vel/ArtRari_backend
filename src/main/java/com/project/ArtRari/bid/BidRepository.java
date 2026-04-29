package com.project.ArtRari.bid;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface BidRepository extends JpaRepository<Bid, Integer> {
    @EntityGraph(attributePaths = "user")
    List<Bid> findByLotId(Long lotId);

    @EntityGraph(attributePaths = {"user"})
    Optional<Bid> findTopByLotIdOrderByAmountDesc(Long lotId);
}
