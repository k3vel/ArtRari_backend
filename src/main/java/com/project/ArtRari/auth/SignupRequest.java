package com.project.ArtRari.auth;

public record SignupRequest(
        String firstName,
        String lastName,
        String phone,
        String email,
        String password
) {}
