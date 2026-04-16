package com.project.ArtRari;

public record SignupRequest(
        String fullName,
        String phone,
        String email,
        String password
) {}
