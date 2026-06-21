package com.project.ArtRari.artwork;


import com.project.ArtRari.artwork.dto.ArtworkUpdateRequest;
import com.project.ArtRari.common.SkipBeforeEach;
import com.project.ArtRari.common.TestDataInitializer;
import org.antlr.v4.runtime.misc.Pair;
import org.junit.jupiter.api.TestInfo;
import tools.jackson.databind.json.JsonMapper;
import com.project.ArtRari.artwork.dto.ArtworkCreateRequest;
import com.project.ArtRari.security.UserDetailsImpl;
import com.project.ArtRari.user.Role;
import com.project.ArtRari.user.User;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
@Transactional
public class ArtworkControllerTestIT {
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private JsonMapper mapper;
    @Autowired
    private ArtworkRepository artworkRepository;
    @Autowired
    private TestDataInitializer testDataInitializer;

    private User testUser;
    private UserDetailsImpl testUdi;
    private Artwork savedArtwork;
    private ArtworkUpdateRequest updateRequest;

    @BeforeEach
    void setUp(TestInfo testInfo) {
        Pair<User, UserDetailsImpl> pair = testDataInitializer.createAndSaveUser("test", Role.user);
        testUser = pair.a;
        testUdi = pair.b;

        boolean shouldSkip = testInfo.getTestMethod()
                .map(method -> method.isAnnotationPresent(SkipBeforeEach.class))
                .orElse(false);
        if (shouldSkip) {
            return;
        }

        savedArtwork = testDataInitializer.createAndSaveArtwork(testUser);

        updateRequest = new ArtworkUpdateRequest("New Title", "Author",
                "Description", "Technique", List.of(), LocalDate.now(),
                "https://photo.com", new BigDecimal("1.1"));
    }

    @Test
    public void getArtwork_ShouldReturn200AndArtwork() throws Exception {
        //todo work status on auction on exhibition ...
        mockMvc.perform(get("/artworks/" + savedArtwork.getId())
                .with(user(testUdi)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Title"));

    }

    @Test
    public void getArtwork_ShouldReturn404_WhenWorkDoesNotExist() throws Exception {
        mockMvc.perform(get("/artworks/999")
                .with(user(testUdi)))
                .andExpect(status().isNotFound());
    }

    @Test
    @SkipBeforeEach
    public void addArtwork_ShouldReturn201() throws Exception {
        ArtworkCreateRequest createRequest = new ArtworkCreateRequest("Title", "Author", "Description",
                "Technique", List.of(), LocalDate.now(), "https://photo.com", new BigDecimal("1.1"));
        String request = mapper.writeValueAsString(createRequest);
        mockMvc.perform(post("/artworks")
                        .with(user(testUdi))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title").value("Title"));
        List<Artwork> artworks = artworkRepository.findAll();
        assertEquals(1, artworks.size());
        assertEquals("Title", artworks.getFirst().getTitle());
        assertEquals(testUser.getId(), artworks.getFirst().getOwner().getId());
    }

    @Test
    public void updateArtwork_ShouldReturn200AndArtwork() throws Exception {
        String request = mapper.writeValueAsString(updateRequest);
        mockMvc.perform(put("/artworks/" + savedArtwork.getId())
                        .with(user(testUdi))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("New Title"));
        List<Artwork> artworks = artworkRepository.findAll();
        assertEquals(1, artworks.size());
        assertEquals("New Title", artworks.getFirst().getTitle());
        assertEquals(testUser.getId(), artworks.getFirst().getOwner().getId());
    }

    @Test
    public void updateArtwork_ShouldReturn401_WhenUnauthorized() throws Exception {
        String request = mapper.writeValueAsString(updateRequest);
        mockMvc.perform(put("/artworks/" + savedArtwork.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isUnauthorized());
    }

    @Test
    public void updateArtwork_ShouldReturn403_WhenWrongRole() throws Exception {
        UserDetailsImpl testCurator = testDataInitializer.createAndSaveUser("curator", Role.curator).b;
        String request = mapper.writeValueAsString(updateRequest);

        mockMvc.perform(put("/artworks/" + savedArtwork.getId())
                        .with(user(testCurator))
                        .contentType(MediaType.APPLICATION_JSON)
                            .content(request))
                .andExpect(status().isForbidden());
    }

    @Test
    public void deleteArtwork_ShouldReturn200() throws Exception {
        mockMvc.perform(delete("/artworks/" + savedArtwork.getId())
                        .with(user(testUdi)))
                .andExpect(status().isNoContent());
        List<Artwork> artworks = artworkRepository.findAll();
        assertEquals(0, artworks.size());
    }

    @Test
    public void deleteArtwork_ShouldReturn401_WhenUnauthorized() throws Exception {
        mockMvc.perform(delete("/artworks/" + savedArtwork.getId()))
                .andExpect(status().isUnauthorized());
    }

    @Test
    public void deleteArtwork_ShouldReturn403_WhenWrongRole() throws Exception {
        UserDetailsImpl testCurator = testDataInitializer.createAndSaveUser("curator", Role.curator).b;

        mockMvc.perform(delete("/artworks/" + savedArtwork.getId())
                        .with(user(testCurator)))
                .andExpect(status().isForbidden());
    }

    @Test
    public void getAvailableArtworks_ShouldReturn200() throws Exception {
        UserDetailsImpl testCurator = testDataInitializer.createAndSaveUser("curator", Role.curator).b;

        mockMvc.perform(get("/artworks?page=0")
                        .with(user(testCurator)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.totalPages").value(1))
                .andExpect(jsonPath("$.items[0].title").value("Title"));
    }

    @Test
    public void getAvailableArtworks_ShouldReturn401_WhenUnauthorized() throws Exception {
        mockMvc.perform(get("/artworks?page=0")).andExpect(status().isUnauthorized());
    }

    @Test
    public void getAvailableArtworks_ShouldReturn403_WhenWrongRole() throws Exception {
        UserDetailsImpl testCurator = testDataInitializer.createAndSaveUser("curator", Role.user).b;

        mockMvc.perform(get("/artworks?page=0")
                        .with(user(testCurator)))
                .andExpect(status().isForbidden());
    }

    @Test
    public void getAvailableArtworks_ShouldReturn200AndEmptyPage() throws Exception {
        savedArtwork.setStatus(WorkStatus.sold);
        artworkRepository.save(savedArtwork);
        UserDetailsImpl testCurator = testDataInitializer.createAndSaveUser("curator", Role.curator).b;

        mockMvc.perform(get("/artworks?page=0")
                        .with(user(testCurator)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(0))
                .andExpect(jsonPath("$.totalPages").value(0));
        List<Artwork> artworks = artworkRepository.findAll();
        assertEquals(1, artworks.size());
    }

    @Test
    public void getMyArtworks_ShouldReturn200() throws Exception {
        mockMvc.perform(get("/artworks/my")
                        .with(user(testUdi)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.totalPages").value(1));
    }

    @Test
    public void getMyArtwork_ShouldReturn200() throws Exception {
        mockMvc.perform(get("/artworks/my/" + savedArtwork.getId())
                        .with(user(testUdi)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Title"));
    }

    @Test
    public void getMyArtwork_ShouldReturn403_WhenNotMyWork() throws Exception {
        User anotherUser = testDataInitializer.createAndSaveUser("another", Role.user).a;
        savedArtwork.setOwner(anotherUser);
        artworkRepository.save(savedArtwork);

        mockMvc.perform(get("/artworks/my/" + savedArtwork.getId())
                        .with(user(testUdi)))
                .andExpect(status().isForbidden());
    }
}
