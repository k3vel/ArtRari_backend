package com.project.ArtRari.user;

import com.project.ArtRari.exception.ArtrariException;
import com.project.ArtRari.security.UserDetailsImpl;
import com.project.ArtRari.user.dto.PasswordChangeRequest;
import com.project.ArtRari.user.dto.ProfileResponse;
import com.project.ArtRari.user.dto.ProfileUpdateRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {
    @Mock
    private UserRepository userRepository;
    @Mock
    private UserMapper userMapper;
    @Mock
    private PasswordEncoder passwordEncoder;
    @InjectMocks
    private UserService userService;

    private User testUser;
    private UserDetailsImpl udiMock;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setEmail("test@gmail.com");
        testUser.setPhone("123456789");
        testUser.setPassword("encodedOldPassword");

        udiMock = new UserDetailsImpl(1L, "John", "Doe", "john", "test@gmail.com",
                "encodedOldPassword", Role.user, false);
    }

    @Test
    void loadUserByUsername_ShouldSucceed() {
        when(userRepository.findByEmail("test@gmail.com")).thenReturn(Optional.of(testUser));
        UserDetails actualDetails = userService.loadUserByUsername("test@gmail.com");

        assertNotNull(actualDetails);
        assertEquals("test@gmail.com", actualDetails.getUsername());
    }

    @Test
    void loadUserByUsername_ShouldThrowNotFound_WhenUserNotFound() {
        when(userRepository.findByEmail("wrong@gmail.com")).thenReturn(Optional.empty());
        assertThrows(UsernameNotFoundException.class, () -> userService.loadUserByUsername("wrong@gmail.com"));
    }

    @Test
    void getProfile_ShouldSucceed() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        ProfileResponse mockResponse = mock(ProfileResponse.class);
        when(userMapper.toProfileResponse(testUser)).thenReturn(mockResponse);
        ProfileResponse actual = userService.getProfile(udiMock);

        assertEquals(mockResponse, actual);
    }

    @Test
    void updateProfile_ShouldSucceed() {
        ProfileUpdateRequest request = new ProfileUpdateRequest("NewFirst", "NewLast",
                "987654321", "new@gmail.com");
        when(userRepository.existsByEmail("new@gmail.com")).thenReturn(false);
        when(userRepository.existsByPhone("987654321")).thenReturn(false);
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        ProfileResponse mockResponse = mock(ProfileResponse.class);
        when(userMapper.toProfileResponse(testUser)).thenReturn(mockResponse);
        ProfileResponse actual = userService.updateProfile(udiMock, request);

        assertEquals("NewFirst", testUser.getFirstName());
        assertEquals("new@gmail.com", testUser.getEmail());
        assertEquals(mockResponse, actual);
    }

    @Test
    void updateProfile_ShouldThrowConflict_WhenEmailExists() {
        ProfileUpdateRequest request = new ProfileUpdateRequest("First", "Last",
                "123456789", "busy@gmail.com");
        when(userRepository.existsByEmail("busy@gmail.com")).thenReturn(true);

        ArtrariException ex = assertThrows(ArtrariException.class, () -> userService.updateProfile(udiMock, request));
        assertEquals(HttpStatus.CONFLICT, ex.getStatus());
    }

    @Test
    void changePassword_ShouldSucceed() {
        PasswordChangeRequest request = new PasswordChangeRequest("oldPass", "newPass");
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches("oldPass", "encodedOldPassword")).thenReturn(true);
        when(passwordEncoder.encode("newPass")).thenReturn("encodedNewPassword");
        userService.changePassword(udiMock, request);

        assertEquals("encodedNewPassword", testUser.getPassword());
    }

    @Test
    void changePassword_ShouldThrowBadRequest_WhenOldPasswordIsWrong() {
        PasswordChangeRequest request = new PasswordChangeRequest("wrongOldPass", "newPass");
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches("wrongOldPass", "encodedOldPassword")).thenReturn(false);

        ArtrariException ex = assertThrows(ArtrariException.class, () -> userService.changePassword(udiMock, request));
        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatus());
    }
}
