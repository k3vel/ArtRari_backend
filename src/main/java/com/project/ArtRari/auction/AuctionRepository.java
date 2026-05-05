package com.project.ArtRari.auction;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface AuctionRepository extends JpaRepository<Auction, Long> {
    @EntityGraph(attributePaths = {"exhibition"})
    List<Auction> findByEndDateAfterOrderByStartDateDesc(Instant now);

    @EntityGraph(attributePaths = {"exhibition"})
    List<Auction> findByStatusOrStatus(AuctionStatus status1, AuctionStatus status2);

    @Query(value = """
            SELECT DISTINCT a.*
            FROM auction AS a
            JOIN lot AS l ON l.auction_id=a.id
            JOIN work AS w ON w.id=l.work_id
            JOIN tag_work AS tw ON tw.work_id=w.id
            JOIN tag AS t ON tw.tag_id=t.id
            WHERE t.name IN :tags
            AND (a.status='active'::auction_status
                 OR a.status='scheduled'::auction_status)""", nativeQuery = true)
    List<Auction> findByTags(@Param("tags") List<String> tags);

    @Override
    @EntityGraph(attributePaths = {"lots", "lots.artwork", "lots.artwork.user", "exhibition"})
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
            JOIN exhibition AS e ON a.exhibition_id = e.id
            WHERE a.status='active'::auction_status
            AND a.end_date<=:now""", nativeQuery = true)
    List<Auction> findAuctionsToClose(@Param("now") Instant now);

    List<Auction> findByExhibitionCuratorId(Long exhibitionCuratorId);
}
