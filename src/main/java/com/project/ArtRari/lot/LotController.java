package com.project.ArtRari.lot;

import com.project.ArtRari.lot.dto.LotResponse;
import com.project.ArtRari.security.UserDetailsImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/lots")
@RequiredArgsConstructor
public class LotController {
    private final LotService lotService;

    @GetMapping("/{id}")
    public ResponseEntity<LotResponse> getLot(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetailsImpl udi
    ) {
        return ResponseEntity.ok(lotService.getById(id, udi));
    }

}
