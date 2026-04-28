package com.project.ArtRari.artwork;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ArtworkRepository extends JpaRepository<Artwork, Long> {
    List<Artwork> findByStatus(WorkStatus status);

}
