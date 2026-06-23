package com.project.ArtRari.user;

import com.project.ArtRari.common.TestDataInitializer;
import com.project.ArtRari.security.UserDetailsImpl;
import com.project.ArtRari.user.dto.PasswordChangeRequest;
import com.project.ArtRari.user.dto.ProfileUpdateRequest;
import jakarta.transaction.Transactional;
import org.antlr.v4.runtime.misc.Pair;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.json.JsonMapper;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
@Transactional
public class UserControllerTestIT {
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private JsonMapper jsonMapper;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private TestDataInitializer testDataInitializer;

    private User testUser;
    private UserDetailsImpl testUdi;


    @BeforeEach
    public void setUp() {
        Pair<User, UserDetailsImpl> pair = testDataInitializer.createAndSaveUser("user", Role.user);
        testUser = pair.a;
        testUdi = pair.b;
    }

    @Test
    public void getProfile_ShouldReturn200() throws Exception {
        mockMvc.perform(get("/profile")
                        .with(user(testUdi)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.firstName").value("User"));
    }

    @Test
    public void updateProfile_ShouldReturn200() throws Exception {
        ProfileUpdateRequest updateRequest = new ProfileUpdateRequest("NewFirst", "NewLast",
                "NewPhone", "NewEmail@gmail.com");
        String request = jsonMapper.writeValueAsString(updateRequest);
        mockMvc.perform(put("/profile")
                        .with(user(testUdi))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.firstName").value("NewFirst"));
    }

    @Test
    public void updateProfile_ShouldReturn409_WhenEmailAlreadyExists() throws Exception {
        User user = testDataInitializer.createAndSaveUser("abc", Role.admin).a;
        ProfileUpdateRequest updateRequest = new ProfileUpdateRequest("NewFirst", "NewLast",
                "NewPhone", user.getEmail());
        String request = jsonMapper.writeValueAsString(updateRequest);

        mockMvc.perform(put("/profile")
                        .with(user(testUdi))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isConflict());
    }

    @Test
    public void changePassword_ShouldReturn204() throws Exception {
        PasswordChangeRequest changeRequest = new PasswordChangeRequest("password", "newPassword");
        String request = jsonMapper.writeValueAsString(changeRequest);
        mockMvc.perform(post("/profile/password")
                        .with(user(testUdi))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isNoContent());
    }

    @Test
    public void changePassword_ShouldReturn400_WhenWrongOldPassword() throws Exception {
        PasswordChangeRequest chRequest = new PasswordChangeRequest("wrongOldPassword", "newPassword");
        String request = jsonMapper.writeValueAsString(chRequest);
        mockMvc.perform(post("/profile/password")
                        .with(user(testUdi))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isBadRequest());
    }
}
