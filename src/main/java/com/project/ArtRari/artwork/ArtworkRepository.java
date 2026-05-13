package com.project.ArtRari.artwork;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ArtworkRepository extends JpaRepository<Artwork, Long> {
    List<Artwork> findByStatus(WorkStatus status);

    Page<Artwork> findByStatusAndExhibitionIsNull(WorkStatus status, Pageable pageable);

    Page<Artwork> findByOwnerId(Long id, Pageable pageable);

    int countAllByOwnerId(Long id);

}
