package com.project.ArtRari.purchase;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PurchaseRepository extends JpaRepository<Purchase, Long> {
    @EntityGraph(attributePaths = {"lot", "lot.artwork"})
    List<Purchase> findByUserId(Long id);

    Optional<Purchase> findByLotId(Long id);
}
