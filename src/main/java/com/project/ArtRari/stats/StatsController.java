package com.project.ArtRari.stats;

import com.project.ArtRari.security.UserDetailsImpl;
import com.project.ArtRari.stats.dto.StatsResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/stats")
@RequiredArgsConstructor
public class StatsController {
    private final StatsService statsService;

    @GetMapping
    @PreAuthorize("hasAnyRole('user', 'curator', 'admin')")
    public ResponseEntity<StatsResponse> getStats(@AuthenticationPrincipal UserDetailsImpl udi) {
        return ResponseEntity.ok(statsService.getStats(udi));
    }
}
