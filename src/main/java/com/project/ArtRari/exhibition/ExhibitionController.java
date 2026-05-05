package com.project.ArtRari.exhibition;

import com.project.ArtRari.exhibition.dto.*;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/exhibitions")
public class ExhibitionController {
    private final ExhibitionService exhibitionService;

    @GetMapping
    public ExhibitionsPageResponse getAll(@RequestParam(required = false) List<String> tags) {
        return exhibitionService.getExhibitions(tags);
    }

    @GetMapping("/{id}")
    public ExhibitionResponse getById(@PathVariable Long id) {
        return exhibitionService.getById(id);
    }

    @PostMapping
    @PreAuthorize("hasRole('curator')")
    public ExhibitionResponse createExhibition(@RequestBody ExhibitionCreateRequest exhibitionCreateRequest) {
        return exhibitionService.createExhibition(exhibitionCreateRequest);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('curator')")
    public ExhibitionResponse updateExhibition(@PathVariable Long id, @RequestBody ExhibitionUpdateRequest exhibitionUpdateRequest) {
        return exhibitionService.updateExhibition(id, exhibitionUpdateRequest);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('curator')")
    public void deleteExhibition(@PathVariable Long id) {
        exhibitionService.deleteExhibition(id);
    }

    @GetMapping("/my")
    @PreAuthorize("hasRole('curator')")
    public List<ExhibitionPreviewResponse> getMyExhibitions() {
        return exhibitionService.getMyExhibitions();
    }

}
