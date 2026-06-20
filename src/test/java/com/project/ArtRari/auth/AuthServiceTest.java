package com.project.ArtRari.auth;

import com.project.ArtRari.exception.ArtrariException;
import com.project.ArtRari.security.JwtService;
import com.project.ArtRari.security.UserDetailsImpl;
import com.project.ArtRari.user.Role;
import com.project.ArtRari.user.User;
import com.project.ArtRari.user.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AuthServiceTest {
    @Mock
    private UserRepository userRepository;
    @Mock
    private AuthenticationManager authenticationManager;
    @Mock
    private JwtService jwtService;
    @Mock
    private PasswordEncoder passwordEncoder;
    @InjectMocks
    private AuthService authService;

    private SigninRequest signinRequest;
    private SignupRequest signupRequest;
    private UserDetailsImpl testUdi;

    @BeforeEach
    void setUp() {
        signinRequest = new SigninRequest("test@mail.com", "password123");

        signupRequest = new SignupRequest("John", "Doe", "123456789", "test@mail.com",
                "password123");

        testUdi = new UserDetailsImpl(1L, "John", "Doe", "123456789", "test@mail.com",
                "encoded_pass", Role.user, false);
    }

    @Test
    void signIn_ShouldSucceed() {
        Authentication mockAuthentication = mock(Authentication.class);
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(mockAuthentication);
        when(mockAuthentication.getPrincipal()).thenReturn(testUdi);
        when(jwtService.generateToken(mockAuthentication)).thenReturn("token");
        AuthResponse res = authService.signIn(signinRequest);

        assertNotNull(res);
        assertEquals("token", res.jwtToken());
        assertEquals("John", res.firstName());
        assertEquals("Doe", res.lastName());
        assertEquals("user", res.role());
        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(jwtService).generateToken(mockAuthentication);
    }

    @Test
    void signIn_ShouldThrowUnauthorized_WhenBadCredentials() {
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new BadCredentialsException("Bad credentials"));

        ArtrariException exception = assertThrows(
                ArtrariException.class,
                () -> authService.signIn(signinRequest)
        );
        assertEquals(HttpStatus.UNAUTHORIZED, exception.getStatus());
        verify(jwtService, never()).generateToken(any());
    }

    @Test
    void signUp_ShouldSucceed() {
        when(userRepository.existsByEmail(signupRequest.email())).thenReturn(false);
        when(userRepository.existsByPhone(signupRequest.phone())).thenReturn(false);
        when(userRepository.existsByFirstNameAndLastName(signupRequest.firstName(), signupRequest.lastName()))
                .thenReturn(false);
        when(passwordEncoder.encode(signupRequest.password())).thenReturn("hashed-password");
        authService.signUp(signupRequest);

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());
        User savedUser = userCaptor.getValue();
        assertEquals("John", savedUser.getFirstName());
        assertEquals("Doe", savedUser.getLastName());
        assertEquals("test@mail.com", savedUser.getEmail());
        assertEquals("123456789", savedUser.getPhone());
        assertEquals("hashed-password", savedUser.getPassword());
        assertEquals(Role.user, savedUser.getRole());
    }

    @Test
    void signUp_ShouldThrowBadRequest_WhenEmailTaken() {
        when(userRepository.existsByEmail(signupRequest.email())).thenReturn(true);
        ArtrariException exception = assertThrows(
                ArtrariException.class,
                () -> authService.signUp(signupRequest)
        );
        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatus());
    }

    @Test
    void signUp_ShouldThrowBadRequest_WhenPhoneTaken() {
        when(userRepository.existsByEmail(signupRequest.email())).thenReturn(false);
        when(userRepository.existsByPhone(signupRequest.phone())).thenReturn(true);
        ArtrariException exception = assertThrows(
                ArtrariException.class,
                () -> authService.signUp(signupRequest)
        );
        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatus());
    }

    @Test
    void signUp_ShouldThrowBadRequest_WhenNameTaken() {
        when(userRepository.existsByEmail(signupRequest.email())).thenReturn(false);
        when(userRepository.existsByPhone(signupRequest.phone())).thenReturn(false);
        when(userRepository.existsByFirstNameAndLastName(signupRequest.firstName(), signupRequest.lastName()))
                .thenReturn(true);
        ArtrariException exception = assertThrows(
                ArtrariException.class,
                () -> authService.signUp(signupRequest)
        );
        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatus());
    }
}
