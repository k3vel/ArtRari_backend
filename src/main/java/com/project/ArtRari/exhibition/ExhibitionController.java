package com.project.ArtRari.exhibition;

import com.project.ArtRari.artwork.tag.Tag;
import com.project.ArtRari.exhibition.dto.ExhibitionPreviewResponse;
import com.project.ArtRari.exhibition.dto.ExhibitionResponse;
import com.project.ArtRari.exhibition.dto.ExhibitionsPageResponse;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@AllArgsConstructor
@RequestMapping("/exhibitions")
public class ExhibitionController {
    private ExhibitionService exhibitionService;

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
