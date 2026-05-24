package com.project.ArtRari.auction;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Repository
public interface AuctionRepository extends JpaRepository<Auction, Long> {
    @EntityGraph(attributePaths = {"exhibition"})
    List<Auction> findByEndDateAfterOrderByStartDateDesc(Instant now);

    @EntityGraph(attributePaths = {"exhibition"})
    Page<Auction> findByStatusOrStatus(AuctionStatus status1, AuctionStatus status2, Pageable pageable );

    @Query("""
            SELECT DISTINCT a
            FROM Auction AS a
            JOIN a.lots AS l
            JOIN l.artwork AS w
            JOIN w.tags AS t
            WHERE t.name IN :tags
            AND a.status IN ('active', 'scheduled')""")
    Page<Auction> findByTags(@Param("tags") List<String> tags, Pageable pageable);

    @Override
    @EntityGraph(attributePaths = {"lots", "lots.artwork", "lots.artwork.owner", "exhibition"})
    Optional<Auction> findById(Long id);

    boolean existsByExhibitionId(Long exhibitionId);

    @Query(value = """
            SELECT a.*
            FROM auction AS a
            JOIN exhibition AS e ON a.exhibition_id = e.id
            WHERE a.status='scheduled'::auction_status
            AND a.start_date<=:now""", nativeQuery = true)
    List<Auction> findAuctionsToOpen(@Param("now") Instant now);

    @Query(value = """
            SELECT a.*
            FROM auction AS a
            WHERE a.status='active'::auction_status
            AND a.end_date<=:now""", nativeQuery = true)
    List<Auction> findAuctionsToClose(@Param("now") Instant now);

    Page<Auction> findByExhibitionCuratorId(Long exhibitionCuratorId, Pageable pageable);

    int countAllByExhibitionCuratorId(Long exhibitionCuratorId);
}
