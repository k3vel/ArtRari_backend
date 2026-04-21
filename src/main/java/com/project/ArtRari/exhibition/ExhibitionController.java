package com.project.ArtRari.exhibition;

import com.project.ArtRari.exhibition.dto.ExhibitionResponse;
import com.project.ArtRari.exhibition.dto.ExhibitionsPageResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/exhibitions")
public class ExhibitionController {
    private final ExhibitionService exhibitionService;

    @GetMapping
    public ExhibitionsPageResponse getAll(
            @RequestParam(required = false) List<String> tags,
            @RequestParam(required = false) String search
    ) {
        return exhibitionService.getExhibitions(tags, search);
    }

    @GetMapping("/{id}")
    public ExhibitionResponse getById(@PathVariable Long id) {
        return exhibitionService.getById(id);
    }


}
