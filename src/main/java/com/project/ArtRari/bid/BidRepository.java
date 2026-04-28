package com.project.ArtRari.bid;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BidRepository extends JpaRepository<Bid, Integer> {
    @EntityGraph(attributePaths = "user")
    List<Bid> findByLotId(Long lotId);
}
