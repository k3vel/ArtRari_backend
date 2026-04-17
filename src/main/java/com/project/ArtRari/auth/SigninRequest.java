package com.project.ArtRari.auth;

public record SigninRequest(
        String email,
        String password
) {}
