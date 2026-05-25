package com.project.ArtRari.user;

import com.project.ArtRari.security.UserDetailsImpl;
import com.project.ArtRari.user.dto.PasswordChangeRequest;
import com.project.ArtRari.user.dto.ProfileResponse;
import com.project.ArtRari.user.dto.ProfileUpdateRequest;
import com.project.ArtRari.stats.dto.StatsResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/profile")
@PreAuthorize("hasAnyRole('user', 'curator', 'admin')")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;

    @GetMapping
    public ResponseEntity<ProfileResponse> getProfile(@AuthenticationPrincipal UserDetailsImpl udi) {
        return ResponseEntity.ok(userService.getProfile(udi));
    }

    @PutMapping
    public ResponseEntity<ProfileResponse> updateProfile(
            @AuthenticationPrincipal UserDetailsImpl udi,
            @Valid @RequestBody ProfileUpdateRequest profileUpdateRequest
    ) {
        ProfileResponse profileResponse = userService.updateProfile(udi, profileUpdateRequest);
        return ResponseEntity.ok(profileResponse);
    }

    @PostMapping("/password")
    public ResponseEntity<?> changePassword(
            @AuthenticationPrincipal UserDetailsImpl udi,
            @Valid @RequestBody PasswordChangeRequest passwordChangeRequest
    ) {
        userService.changePassword(udi, passwordChangeRequest);
        return ResponseEntity.noContent().build();
    }

}
