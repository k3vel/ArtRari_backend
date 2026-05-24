package com.project.ArtRari.artwork;

import com.project.ArtRari.artwork.dto.*;
import com.project.ArtRari.common.PageResponse;
import com.project.ArtRari.security.UserDetailsImpl;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

@RestController
@RequestMapping("/artworks")
@RequiredArgsConstructor
public class ArtworkController {
    private final ArtworkService artworkService;

    @GetMapping("/{id}")
    public ResponseEntity<ArtworkResponse> getArtwork(@PathVariable Long id) {
        return ResponseEntity.ok(artworkService.getById(id));
    }

    @PostMapping
    @PreAuthorize("hasRole('user')")
    public ResponseEntity<ArtworkResponse> addArtwork(
            @Valid @RequestBody ArtworkCreateRequest artworkCreateRequest,
            @AuthenticationPrincipal UserDetailsImpl udi
    ) {
        ArtworkResponse artworkResponse = artworkService.addArtwork(artworkCreateRequest, udi);
        URI uri = URI.create("/artworks/" + artworkResponse.id());
        return ResponseEntity.created(uri).body(artworkResponse);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('user')")
    public ResponseEntity<ArtworkResponse> updateArtwork(
            @PathVariable Long id,
            @Valid @RequestBody ArtworkUpdateRequest artworkUpdateRequest,
            @AuthenticationPrincipal UserDetailsImpl udi
    ) {
        ArtworkResponse artworkResponse = artworkService.updateArtwork(id, artworkUpdateRequest, udi);
        return ResponseEntity.ok(artworkResponse);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('user')")
    public ResponseEntity<?> deleteArtwork(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetailsImpl udi
    ) {
        artworkService.deleteArtwork(id, udi);
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('curator', 'admin')")
    public ResponseEntity<PageResponse<ArtworkAdvancedPreviewResponse>> getAvailableArtworks(
            @RequestParam(defaultValue = "0") int page
    ) {
        PageResponse<ArtworkAdvancedPreviewResponse> pageResponse = artworkService.getAvailableArtworks(page);
        return ResponseEntity.ok(pageResponse);
    }

    @GetMapping("/my")
    @PreAuthorize("hasRole('user')")
    public ResponseEntity<PageResponse<ArtworkPreviewResponse>> getMyArtworks(
            @RequestParam(defaultValue = "0") int page,
            @AuthenticationPrincipal UserDetailsImpl udi
    ) {
        PageResponse<ArtworkPreviewResponse> pageResponse = artworkService.getMyArtworks(page, udi);
        return ResponseEntity.ok(pageResponse);
    }

    @GetMapping("/my/{id}")
    @PreAuthorize("hasRole('user')")
    public ResponseEntity<MyArtworkResponse> getMyArtwork(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetailsImpl udi) {
        return ResponseEntity.ok(artworkService.getMyArtwork(id, udi));
    }
}
