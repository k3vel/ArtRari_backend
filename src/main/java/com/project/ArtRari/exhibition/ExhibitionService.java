package com.project.ArtRari.exhibition;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@AllArgsConstructor
public class ExhibitionService {
    private ExhibitionRepository exhibitionRepository;

    public List<Exhibition> getAll() {
        return exhibitionRepository.findAll();
    }

    public List<Exhibition> getTop6() {
        return exhibitionRepository.findTop6ByOrderByIdDesc();
    }
}
