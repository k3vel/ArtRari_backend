package com.project.ArtRari.exhibition;

import com.project.ArtRari.common.PageResponse;
import com.project.ArtRari.exhibition.dto.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/exhibitions")
public class ExhibitionController {
    private final ExhibitionService exhibitionService;

    @GetMapping
    public ExhibitionsPageResponse getAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(required = false) List<String> tags
    ) {
        return exhibitionService.getExhibitions(page, tags);
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
    public ResponseEntity<?> deleteExhibition(@PathVariable Long id) {
        exhibitionService.deleteExhibition(id);
        return ResponseEntity.ok("Виставку успішно видалено");
    }

    @GetMapping("/my")
    @PreAuthorize("hasRole('curator')")
    public PageResponse<ExhibitionPreviewResponse> getMyExhibitions(@RequestParam(defaultValue = "0") int page) {
        return exhibitionService.getMyExhibitions(page);
    }

}
