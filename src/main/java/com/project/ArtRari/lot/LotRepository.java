package com.project.ArtRari.lot;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Repository
public interface LotRepository extends JpaRepository<Lot, Long> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @EntityGraph(attributePaths = {"artwork", "auction", "artwork.owner"})
    @Query("SELECT l FROM Lot l WHERE l.id=:lotId")
    Optional<Lot> findByIdForUpdate(@Param("lotId") Long lotId);

    @EntityGraph(attributePaths = {"artwork"})
    @Query("SELECT l FROM Lot l WHERE l.status=:status AND l.endDate<=:now")
    List<Lot> findLotsToClose(@Param("now") Instant now, @Param("status") LotStatus status);

    boolean existsByArtworkIdAndStatusNot(Long artworkId, LotStatus status);
}
