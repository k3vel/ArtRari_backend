package com.project.ArtRari.exhibition;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ExhibitionRepository extends JpaRepository<Exhibition, Long>, JpaSpecificationExecutor<Exhibition> {
    List<Exhibition> findTop6ByStatusOrderByIdDesc(ExhibitionStatus status);

    @EntityGraph(attributePaths = {"artworks", "curator"})
    Optional<Exhibition> findByIdAndStatus(long id, ExhibitionStatus status);

    Page<Exhibition> findByStatus(ExhibitionStatus status, Specification<Exhibition> spec, Pageable pageable);

    Page<Exhibition> findByCuratorId(Long id, Pageable pageable);

    int countAllByCuratorId(Long id);
}
