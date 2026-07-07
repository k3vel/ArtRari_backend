package com.project.ArtRari.admin;

import com.project.ArtRari.admin.dto.BanStatusRequest;
import com.project.ArtRari.admin.dto.RoleChangeRequest;
import com.project.ArtRari.artwork.dto.ArtworkAdvancedPreviewResponse;
import com.project.ArtRari.common.PageResponse;
import com.project.ArtRari.exhibition.dto.ExhibitionPreviewResponse;
import com.project.ArtRari.security.UserDetailsImpl;
import com.project.ArtRari.user.dto.ProfileResponse;
import com.project.ArtRari.user.dto.UserPreviewResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@PreAuthorize("hasRole('admin')")
@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminPanelController {
    private final AdminPanelService adminPanelService;

    @GetMapping("/users")
    public ResponseEntity<PageResponse<UserPreviewResponse>> getAllUsers(
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "0") int page,
            @AuthenticationPrincipal UserDetailsImpl udi
    ) {
        return ResponseEntity.ok(adminPanelService.getAllUsers(search, page, udi));
    }

    @GetMapping("/users/{id}")
    public ResponseEntity<ProfileResponse> getUser(@PathVariable Long id) {
        return ResponseEntity.ok(adminPanelService.getUserProfile(id));
    }

    @PatchMapping("/users/{id}/ban")
    public ResponseEntity<?> banUser(
            @PathVariable Long id,
            @RequestBody BanStatusRequest request,
            @AuthenticationPrincipal UserDetailsImpl udi
    ) {
        adminPanelService.setBanned(id, request.isBanned(), udi);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/users/{id}/role")
    public ResponseEntity<?> changeRole(
            @PathVariable Long id,
            @RequestBody RoleChangeRequest request,
            @AuthenticationPrincipal UserDetailsImpl udi) {
        adminPanelService.changeRole(id, request.role(), udi);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/artworks/{id}")
    public ResponseEntity<?> deleteArtwork(@PathVariable Long id) {
        adminPanelService.deleteArtwork(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/artworks")
    public ResponseEntity<PageResponse<ArtworkAdvancedPreviewResponse>> getAllArtworks(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) List<String> tags,
            @RequestParam(defaultValue = "0") int page
    ) {
        return ResponseEntity.ok(adminPanelService.getAllArtworks(search, tags, page));
    }

    @GetMapping("/exhibitions")
    public ResponseEntity<PageResponse<ExhibitionPreviewResponse>> getAllExhibitions(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) List<String> tags,
            @RequestParam(defaultValue = "0") int page
    ) {
        return ResponseEntity.ok(adminPanelService.getAllExhibitions(search, tags, page));
    }

    @DeleteMapping("/exhibitions/{id}")
    public ResponseEntity<?> deleteExhibition(@PathVariable Long id) {
        adminPanelService.deleteExhibition(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/lots/{id}")
    public ResponseEntity<?> switchLot(@PathVariable Long id) {
        adminPanelService.cancelLot(id);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/bids/{id}")
    public ResponseEntity<?> deleteBid(@PathVariable Long id) {
        adminPanelService.deleteBid(id);
        return ResponseEntity.noContent().build();
    }
    //todo остановить лот, аукцион

}
