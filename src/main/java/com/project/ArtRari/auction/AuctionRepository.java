package com.project.ArtRari.auction;

import com.project.ArtRari.exhibition.Exhibition;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Repository
public interface AuctionRepository extends JpaRepository<Auction, Long>, JpaSpecificationExecutor<Auction> {
    @EntityGraph(attributePaths = {"exhibition"})
    List<Auction> findByEndDateAfterOrderByStartDateDesc(Instant now);

    @Override
    @EntityGraph(attributePaths = {"lots", "lots.artwork", "lots.artwork.owner", "exhibition"})
    Optional<Auction> findById(Long id);

    boolean existsByExhibitionId(Long exhibitionId);

    @EntityGraph(attributePaths = {"lots", "exhibition"})
    @Query("""
           SELECT a FROM Auction a
           WHERE a.status = 'scheduled'
           AND a.startDate <= :now""")
    List<Auction> findAuctionsToOpen(@Param("now") Instant now);

    @Query(value = """
            SELECT a.*
            FROM auction AS a
            WHERE a.status='active'::auction_status
            AND a.end_date<=:now
            AND NOT EXISTS (
                            SELECT 1
                            FROM lot AS l
                            WHERE l.auction_id=a.id AND l.status='available'::lot_status
                        )""", nativeQuery = true)
    List<Auction> findAuctionsToClose(@Param("now") Instant now);

    Page<Auction> findByExhibitionCuratorId(Long exhibitionCuratorId, Pageable pageable);

    int countAllByExhibitionCuratorId(Long exhibitionCuratorId);
}
