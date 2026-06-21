package com.project.ArtRari.exhibition;

import com.project.ArtRari.artwork.Artwork;
import com.project.ArtRari.common.SkipBeforeEach;
import com.project.ArtRari.exhibition.dto.ExhibitionCreateRequest;
import com.project.ArtRari.exhibition.dto.ExhibitionUpdateRequest;
import com.project.ArtRari.security.UserDetailsImpl;
import com.project.ArtRari.common.TestDataInitializer;
import com.project.ArtRari.user.Role;
import com.project.ArtRari.user.User;
import jakarta.transaction.Transactional;
import org.antlr.v4.runtime.misc.Pair;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.json.JsonMapper;

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
public class ExhibitionControllerTestIT {
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private JsonMapper jsonMapper;
    @Autowired
    private ExhibitionRepository exhibitionRepository;
    @Autowired
    private TestDataInitializer testDataInitializer;

    private User testUser;
    private UserDetailsImpl testUdi;
    private Artwork testArtwork;
    private Exhibition savedExhibition;

    @BeforeEach
    public void setUp(TestInfo testInfo) {
        Pair<User,UserDetailsImpl> pair = testDataInitializer.createAndSaveUser("test", Role.curator);
        testUser = pair.a;
        testUdi = pair.b;

        boolean shouldSkip = testInfo.getTestMethod()
                .map(method -> method.isAnnotationPresent(SkipBeforeEach.class))
                .orElse(false);
        if (shouldSkip) {
            return;
        }

        User artworkOwner = testDataInitializer.createAndSaveUser("abc", Role.user).a;
        testArtwork = testDataInitializer.createAndSaveArtwork(artworkOwner);
        savedExhibition = testDataInitializer.createAndSaveExhibition(testArtwork, testUser);
    }

    @Test
    public void getExhibitionsWithEmptyTags_ShouldReturn200() throws Exception {
        mockMvc.perform(get("/exhibitions?page=0")
                        .with(user(testUdi)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.totalPages").value(1))
                .andExpect(jsonPath("$.items[0].title").value("Exhibition"));
    }

    @Test
    public void getExhibition_ShouldReturn200() throws Exception {
        mockMvc.perform(get("/exhibitions/" + savedExhibition.getId())
                        .with(user(testUdi)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Exhibition"));
    }

    @Test
    @SkipBeforeEach
    public void createExhibition_ShouldReturn201() throws Exception {
        User user = testDataInitializer.createAndSaveUser("abc", Role.user).a;
        Artwork artwork = testDataInitializer.createAndSaveArtwork(user);
        ExhibitionCreateRequest createRequest = new ExhibitionCreateRequest(List.of(artwork.getId()),
                "Title", "Theme", "Description", "https://photo.com", "https://photo.com");
        String request = jsonMapper.writeValueAsString(createRequest);
        mockMvc.perform(post("/exhibitions")
                        .with(user(testUdi))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title").value("Title"));
        List<Exhibition> exhibitions = exhibitionRepository.findAll();
        assertEquals(1, exhibitions.size());
        assertEquals("Title", exhibitions.getFirst().getTitle());
    }

    @Test
    public void createExhibition_ShouldReturn409_WhenArtworkIsAlreadyOnExhibition() throws Exception {
        ExhibitionCreateRequest anotherRequest = new ExhibitionCreateRequest(List.of(testArtwork.getId()),
                "Title", "Theme", "Description", "https://photo.com", "https://photo.com");
        String request = jsonMapper.writeValueAsString(anotherRequest);
        mockMvc.perform(post("/exhibitions")
                        .with(user(testUdi))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isConflict());
    }

    @Test
    public void updateExhibition_ShouldReturn200() throws Exception {
        ExhibitionUpdateRequest updateRequest = new ExhibitionUpdateRequest(List.of(testArtwork.getId()), "New title", "Theme",
                "Description", "https://photo.com", "https://photo.com");
        String request = jsonMapper.writeValueAsString(updateRequest);
        mockMvc.perform(put("/exhibitions/" + savedExhibition.getId())
                        .with(user(testUdi))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("New title"));
    }

    @Test
    public void deleteExhibition_ShouldReturn200() throws Exception {
        mockMvc.perform(delete("/exhibitions/" + savedExhibition.getId())
                        .with(user(testUdi)))
                .andExpect(status().isNoContent());
        List<Exhibition> exhibitions = exhibitionRepository.findAll();
        assertEquals(0, exhibitions.size());
    }

    @Test
    public void deleteExhibition_ShouldReturn409_WhenConvertedIntoAuction() throws Exception {
        savedExhibition.setStatus(ExhibitionStatus.converted_into_auction);
        exhibitionRepository.save(savedExhibition);

        mockMvc.perform(delete("/exhibitions/" + savedExhibition.getId())
                        .with(user(testUdi)))
                .andExpect(status().isConflict());
    }

    @Test
    public void getMyExhibitions_ShouldReturn200() throws Exception {
        mockMvc.perform(get("/exhibitions/my?page=0")
                        .with(user(testUdi)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.totalPages").value(1))
                .andExpect(jsonPath("$.items[0].title").value("Exhibition"));
    }

    @Test
    public void getMyExhibition_ShouldReturn200() throws Exception {
        mockMvc.perform(get("/exhibitions/my/" + savedExhibition.getId())
                        .with(user(testUdi)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Exhibition"));
    }

    @Test
    public void getMyExhibition_ShouldReturn404_WhenNotFound() throws Exception {
        mockMvc.perform(get("/exhibitions/my/999")
                        .with(user(testUdi)))
                .andExpect(status().isNotFound());
    }
}
