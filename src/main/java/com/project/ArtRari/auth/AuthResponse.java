package com.project.ArtRari.auth;

import com.project.ArtRari.user.dto.UserResponse;

public record AuthResponse(
        String jwtToken,
        UserResponse user
) {}
