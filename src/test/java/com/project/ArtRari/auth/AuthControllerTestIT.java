package com.project.ArtRari.auth;

import com.project.ArtRari.security.UserDetailsImpl;
import com.project.ArtRari.user.Role;
import com.project.ArtRari.user.User;
import com.project.ArtRari.user.UserRepository;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.json.JsonMapper;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
@Transactional
public class AuthControllerTestIT {
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private JsonMapper jsonMapper;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;

    private SigninRequest signinRequest;
    private SignupRequest signupRequest;
    private User testUser;

    @BeforeEach
    void setUp() {
        signinRequest = new SigninRequest("test@mail.com", "password123");

        signupRequest = new SignupRequest("John", "Doe", "123456789", "test@mail.com",
                "password123");

        testUser = new User();
        testUser.setFirstName("John");
        testUser.setLastName("Doe");
        testUser.setEmail("test@mail.com");
        testUser.setPhone("123456789");
        testUser.setPassword(passwordEncoder.encode("password123"));
        testUser.setRole(Role.user);
        testUser.setBanned(false);
    }

    @Test
    public void signIn_ShouldReturn200() throws Exception {
        userRepository.save(testUser);
        String request = jsonMapper.writeValueAsString(signinRequest);

        mockMvc.perform(post("/auth/signin")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.firstName").value("John"))
                .andExpect(jsonPath("$.lastName").value("Doe"));
    }

    @Test
    public void signIn_ShouldReturn401_WhenWrongCredentials() throws Exception {
        String request = jsonMapper.writeValueAsString(signinRequest);
        mockMvc.perform(post("/auth/signin")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isUnauthorized());
    }

    @Test
    public void signUp_ShouldReturn200() throws Exception {
        String request = jsonMapper.writeValueAsString(signupRequest);

        mockMvc.perform(post("/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isCreated());
        List<User> users = userRepository.findAll();
        assertEquals(1, users.size());
        assertEquals("John", users.get(0).getFirstName());
    }

    @Test
    public void signUp_ShouldReturn400_WhenDataIsTaken() throws Exception {
        userRepository.save(testUser);
        String request = jsonMapper.writeValueAsString(signupRequest);

        mockMvc.perform(post("/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isBadRequest());
    }

}
