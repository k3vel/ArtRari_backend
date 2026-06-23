package com.project.ArtRari.user;

import com.project.ArtRari.user.dto.ProfileResponse;
import com.project.ArtRari.user.dto.UserPreviewResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class UserMapperTest {
    private UserMapper userMapper;
    private User user;

    public UserMapperTest() {
        userMapper = new UserMapper();
    }

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1L);
        user.setFirstName("John");
        user.setLastName("Doe");
        user.setPhone("+380991234567");
        user.setEmail("john.doe@gmail.com");
    }

    @Test
    void toUserPreviewResponse_ShouldMapCorrectly() {
        UserPreviewResponse response = userMapper.toUserPreviewResponse(user);
        assertEquals(1L, response.id());
        assertEquals("John", response.firstName());
        assertEquals("Doe", response.lastName());
    }

    @Test
    void toProfileResponse_ShouldMapCorrectly() {
        ProfileResponse response = userMapper.toProfileResponse(user);
        assertEquals(1L, response.id());
        assertEquals("John", response.firstName());
        assertEquals("Doe", response.lastName());
        assertEquals("+380991234567", response.phone());
        assertEquals("john.doe@gmail.com", response.email());
    }
}
