package com.project.ArtRari.exhibition;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ExhibitionRepository extends JpaRepository<Exhibition, Long> {
    List<Exhibition> findTop6ByOrderByIdDesc();
}
