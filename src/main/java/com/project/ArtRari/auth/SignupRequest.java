package com.project.ArtRari.auth;

public record SignupRequest(
        String fullName,
        String phone,
        String email,
        String password
) {}
