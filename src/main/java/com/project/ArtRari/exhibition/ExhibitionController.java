package com.project.ArtRari.exhibition;

import com.project.ArtRari.common.PageResponse;
import com.project.ArtRari.exhibition.dto.*;
import com.project.ArtRari.security.UserDetailsImpl;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/exhibitions")
public class ExhibitionController {
    private final ExhibitionService exhibitionService;

    @GetMapping
    public ResponseEntity<ExhibitionsPageResponse> getAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(required = false) List<String> tags
    ) {
        return ResponseEntity.ok(exhibitionService.getExhibitions(page, tags));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ExhibitionResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(exhibitionService.getById(id));
    }

    @PostMapping
    @PreAuthorize("hasRole('curator')")
    public ResponseEntity<ExhibitionResponse> createExhibition(
            @Valid @RequestBody ExhibitionCreateRequest exhibitionCreateRequest,
            @AuthenticationPrincipal UserDetailsImpl udi
    ) {
        ExhibitionResponse response = exhibitionService.createExhibition(exhibitionCreateRequest, udi);
        URI uri = URI.create("/exhibitions/" + response.id());
        return ResponseEntity.created(uri).body(response);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('curator')")
    public ResponseEntity<ExhibitionResponse> updateExhibition(
            @PathVariable Long id,
            @Valid @RequestBody ExhibitionUpdateRequest exhibitionUpdateRequest,
            @AuthenticationPrincipal UserDetailsImpl udi
    ) {
        ExhibitionResponse response = exhibitionService.updateExhibition(id, exhibitionUpdateRequest, udi);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('curator')")
    public ResponseEntity<?> deleteExhibition(@PathVariable Long id, @AuthenticationPrincipal UserDetailsImpl udi) {
        exhibitionService.deleteExhibition(id, udi);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/my")
    @PreAuthorize("hasRole('curator')")
    public ResponseEntity<PageResponse<ExhibitionPreviewResponse>> getMyExhibitions(
            @RequestParam(defaultValue = "0") int page,
            @AuthenticationPrincipal UserDetailsImpl udi
    ) {
        return ResponseEntity.ok(exhibitionService.getMyExhibitions(page, udi));
    }

    @GetMapping("/my/{id}")
    @PreAuthorize("hasRole('curator')")
    public ResponseEntity<ExhibitionAdvancedResponse> getMyExhibition(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetailsImpl udi
            ) {
        return ResponseEntity.ok(exhibitionService.getMyExhibitionAdvanced(id, udi));
    }

}
