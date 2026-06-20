package com.project.ArtRari.auth;

import com.project.ArtRari.user.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class SecurityController {
    private final AuthService authService;

    @PostMapping("/signin")
    ResponseEntity<AuthResponse> signin(@Valid @RequestBody SigninRequest signinRequest) {
        AuthResponse authResponse = authService.signIn(signinRequest);
        return ResponseEntity.ok(authResponse);
    }

    @PostMapping("/signup")
    ResponseEntity<?> signup(@Valid @RequestBody SignupRequest signupRequest) {
        authService.signUp(signupRequest);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }
}
