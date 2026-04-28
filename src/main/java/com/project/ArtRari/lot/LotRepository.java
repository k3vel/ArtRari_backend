package com.project.ArtRari.lot;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface LotRepository extends JpaRepository<Lot, Long> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT l FROM Lot l WHERE l.id=:lotId")
    Optional<Lot> findByIdForUpdate(@Param("lotId") Long lotId);
}
