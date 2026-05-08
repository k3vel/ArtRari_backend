package com.project.ArtRari.auth;

import com.project.ArtRari.exception.ArtrariException;
import com.project.ArtRari.security.JwtService;
import com.project.ArtRari.security.UserDetailsImpl;
import com.project.ArtRari.user.Role;
import com.project.ArtRari.user.User;
import com.project.ArtRari.user.UserRepository;
import com.project.ArtRari.user.UserService;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
@AllArgsConstructor
public class SecurityController {
    private final UserService userService;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final UserRepository userRepository; //todo remove

    @PostMapping("/signin")
    ResponseEntity<?> signin(@RequestBody SigninRequest signinRequest) {
        Authentication authentication = null;
        try {
            authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(signinRequest.email(), signinRequest.password())
            );
        } catch (BadCredentialsException e) {
            throw new ArtrariException(HttpStatus.UNAUTHORIZED, "Невірна пошта або пароль");
        }
        SecurityContextHolder.getContext().setAuthentication(authentication);
        String jwt = jwtService.generateToken(authentication);
        UserDetailsImpl udi = (UserDetailsImpl) authentication.getPrincipal();
        AuthResponse authResponse = new AuthResponse(
                jwt,
                udi.getId(),
                udi.getFirstName(),
                udi.getLastName(),
                udi.getRole().name()
        );
        return ResponseEntity.ok(authResponse);
    }

    @PostMapping("/signup")
    ResponseEntity<?> signup(@RequestBody SignupRequest signupRequest) {
        if (userRepository.existsByEmail(signupRequest.email()))
            throw new ArtrariException(HttpStatus.BAD_REQUEST, "Ця пошта вже зайнята");
        if (userRepository.existsByPhone(signupRequest.phone()))
            throw new ArtrariException(HttpStatus.BAD_REQUEST, "Цей номер телефона вже зайнято");
        if (userRepository.existsByFirstNameAndLastName(signupRequest.firstName(), signupRequest.lastName()))
            throw new ArtrariException(HttpStatus.BAD_REQUEST, "Користувач з таким ім'ям вже існує");
        User user = new User();
        user.setFirstName(signupRequest.firstName());
        user.setLastName(signupRequest.lastName());
        user.setPhone(signupRequest.phone());
        user.setEmail(signupRequest.email());
        user.setPassword(passwordEncoder.encode(signupRequest.password()));
        user.setRole(Role.user);
        userRepository.save(user);
        return ResponseEntity.ok("Signup successful");
    }
}
