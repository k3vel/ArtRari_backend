package com.project.ArtRari.auth;

import com.project.ArtRari.exception.ArtrariException;
import com.project.ArtRari.security.JwtService;
import com.project.ArtRari.security.UserDetailsImpl;
import com.project.ArtRari.user.Role;
import com.project.ArtRari.user.User;
import com.project.ArtRari.user.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final UserRepository userRepository;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;

    public AuthResponse signIn(SigninRequest signinRequest) {
        Authentication authentication;
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
        return new AuthResponse(
                jwt,
                udi.getId(),
                udi.getFirstName(),
                udi.getLastName(),
                udi.getRole().name()
        );
    }

    @Transactional
    public void signUp(SignupRequest signupRequest) {
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
    }
}
