package com.project.ArtRari.artwork;

import com.project.ArtRari.artwork.dto.ArtworkCreateRequest;
import com.project.ArtRari.artwork.dto.ArtworkPreviewResponse;
import com.project.ArtRari.artwork.dto.ArtworkResponse;
import com.project.ArtRari.artwork.dto.ArtworkUpdateRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/artworks")
@RequiredArgsConstructor
public class ArtworkController {
    private final ArtworkService artworkService;

    @GetMapping("/{id}")
    public ArtworkResponse getArtwork(@PathVariable Long id) {
        return artworkService.getById(id);
    }

    @PostMapping
    @PreAuthorize("hasRole('user')")
    public ArtworkResponse addArtwork(@RequestBody ArtworkCreateRequest artworkCreateRequest) {
        return artworkService.addArtwork(artworkCreateRequest);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('user')")
    public ArtworkResponse updateArtwork(@PathVariable Long id, @RequestBody ArtworkUpdateRequest artworkUpdateRequest) {
        return artworkService.updateArtwork(id, artworkUpdateRequest);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('user')")
    public void deleteArtwork(@PathVariable Long id) {
        artworkService.deleteArtwork(id);
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('curator', 'admin')")
    public List<ArtworkPreviewResponse> getAll() {
        return artworkService.getAvailableArtworks();
    }
}
