package com.project.ArtRari.exhibition;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ExhibitionRepository extends JpaRepository<Exhibition, Long> {
    List<Exhibition> findTop6ByStatusOrderByIdDesc(ExhibitionStatus status);

    @EntityGraph(attributePaths = {"artworks", "curator"})
    Optional<Exhibition> findByIdAndStatus(long id, ExhibitionStatus status);

    Page<Exhibition> findByStatus(ExhibitionStatus status, Pageable pageable);

    @Query(value = """
            SELECT DISTINCT e.*
            FROM exhibition AS e
            JOIN work AS w ON w.exhibition_id=e.id
            JOIN tag_work AS tw ON tw.work_id=w.id
            JOIN tag AS t ON tw.tag_id=t.id
            WHERE t.name IN :tags
            AND e.status='running'::exhibition_status""", nativeQuery = true)
    Page<Exhibition> findByTags(@Param("tags") List<String> tags, Pageable pageable);
/*
    @Query(value = """
            SELECT DISTINCT e.*
            FROM exhibition AS e
            JOIN work AS w ON w.exhibition_id=e.id
            WHERE (:search IS NULL OR LOWER(w.title) LIKE LOWER(CONCAT('%',:search,'%'))
                            OR LOWER(w.author) LIKE LOWER(CONCAT('%',:search,'%'))
                            OR LOWER(w.technique) LIKE LOWER(CONCAT('%',:search,'%')))
            AND e.status='running'::exhibition_status""", nativeQuery = true)
    List<Exhibition> findBySearchOnly(@Param("search") String search);
*/
    Page<Exhibition> findByCuratorId(Long id, Pageable pageable);

}
