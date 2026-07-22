package com.project.ArtRari.artwork;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;

public interface ArtworkRepository extends JpaRepository<Artwork, Long>, JpaSpecificationExecutor<Artwork> {
    List<Artwork> findByStatus(WorkStatus status);

    Page<Artwork> findByStatusAndExhibitionIsNull(WorkStatus status, Pageable pageable);

    Page<Artwork> findByOwnerId(Long id, Pageable pageable);

    int countAllByOwnerId(Long id);

    List<Artwork> findByTagsId(Long id);

    int countAllByStatus(WorkStatus status);
}
