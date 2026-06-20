package com.project.ArtRari.exhibition;

import com.project.ArtRari.artwork.Artwork;
import com.project.ArtRari.artwork.ArtworkRepository;
import com.project.ArtRari.artwork.WorkStatus;
import com.project.ArtRari.exhibition.dto.ExhibitionCreateRequest;
import com.project.ArtRari.exhibition.dto.ExhibitionUpdateRequest;
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
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.json.JsonMapper;

import java.math.BigDecimal;
import java.time.Instant;
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
public class ExhibitionControllerTestIT {
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private JsonMapper jsonMapper;
    @Autowired
    private ExhibitionRepository exhibitionRepository;
    @Autowired
    private ArtworkRepository artworkRepository;
    @Autowired
    private UserRepository userRepository;

    private User testUser;
    private UserDetailsImpl testUdi;
    private Artwork testArtwork;
    private Exhibition testExhibition;
    private ExhibitionCreateRequest createRequest;
    private ExhibitionUpdateRequest updateRequest;

    @BeforeEach
    public void setUp() {
        testUser = new User();
        testUser.setFirstName("John");
        testUser.setLastName("Doe");
        testUser.setEmail("john@doe.com");
        testUser.setPassword("password");
        testUser.setPhone("123456789");
        testUser.setRole(Role.curator);
        userRepository.save(testUser);

        testUdi = new UserDetailsImpl(testUser.getId(), testUser.getFirstName(), testUser.getLastName(),
                testUser.getPhone(), testUser.getEmail(), testUser.getPassword(), testUser.getRole(),
                testUser.isBanned());

        testArtwork = new Artwork();;
        testArtwork.setTitle("Title");
        testArtwork.setOwner(testUser);
        testArtwork.setDescription("Description");
        testArtwork.setTechnique("Technique");
        testArtwork.setStatus(WorkStatus.available);
        testArtwork.setAuthor("Author");
        testArtwork.setTags(List.of());
        testArtwork.setCreationDate(LocalDate.now());
        testArtwork.setPhotoUrl("https://photo.com");
        testArtwork.setStartPrice(new BigDecimal("1.1"));
        Artwork savedArtwork = artworkRepository.save(testArtwork);

        testExhibition = new Exhibition();
        testExhibition.setCurator(testUser);
        testExhibition.setTitle("Exhibition");
        testExhibition.setTheme("Theme");
        testExhibition.setDescription("Description");
        testExhibition.setBackgroundUrl("https://photo.com");
        testExhibition.setStartDate(Instant.now());
        testExhibition.setStatus(ExhibitionStatus.running);
        testExhibition.setThumbnailUrl("https://photo.com");

        createRequest = new ExhibitionCreateRequest(List.of(savedArtwork.getId()), "Title", "Theme",
                "Description", "https://photo.com", "https://photo.com");
        updateRequest = new ExhibitionUpdateRequest(List.of(savedArtwork.getId()), "New title", "Theme",
                "Description", "https://photo.com", "https://photo.com");
    }

    @Test
    public void getExhibitionsWithEmptyTags_ShouldReturn200() throws Exception {
        testExhibition.setArtworks(List.of(testArtwork));
        exhibitionRepository.save(testExhibition);

        mockMvc.perform(get("/exhibitions?page=0")
                        .with(user(testUdi)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.totalPages").value(1))
                .andExpect(jsonPath("$.items[0].title").value("Exhibition"));
    }

    @Test
    public void getExhibition_ShouldReturn200() throws Exception {
        testExhibition.setArtworks(List.of(testArtwork));
        Exhibition saved = exhibitionRepository.save(testExhibition);

        mockMvc.perform(get("/exhibitions/" + saved.getId())
                        .with(user(testUdi)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Exhibition"));
    }

    @Test
    public void getExhibition_ShouldReturn404_WhenNotFound() throws Exception {
        mockMvc.perform(get("/exhibitions/1")
                        .with(user(testUdi)))
                .andExpect(status().isNotFound());
    }

    @Test
    public void createExhibition_ShouldReturn201() throws Exception {
        String request = jsonMapper.writeValueAsString(createRequest);
        mockMvc.perform(post("/exhibitions")
                        .with(user(testUdi))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title").value("Title"));
        List<Exhibition> exhibitions = exhibitionRepository.findAll();
        assertEquals(1, exhibitions.size());
        assertEquals("Title", exhibitions.get(0).getTitle());
    }

    @Test
    public void createExhibition_ShouldReturn409_WhenArtworkIsAlreadyOnExhibition() throws Exception {
        testExhibition.setArtworks(List.of(testArtwork));
        exhibitionRepository.save(testExhibition);
        String request = jsonMapper.writeValueAsString(createRequest);

        mockMvc.perform(post("/exhibitions")
                        .with(user(testUdi))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isConflict());
    }

    @Test
    public void updateExhibition_ShouldReturn200() throws Exception {
        testExhibition.setArtworks(List.of(testArtwork));
        Exhibition saved = exhibitionRepository.save(testExhibition);
        String request = jsonMapper.writeValueAsString(updateRequest);

        mockMvc.perform(put("/exhibitions/" + saved.getId())
                        .with(user(testUdi))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("New title"));
    }

    @Test
    public void updateExhibition_ShouldReturn404_WhenNotFound() throws Exception {
        String request = jsonMapper.writeValueAsString(updateRequest);
        mockMvc.perform(put("/exhibitions/999")
                        .with(user(testUdi))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isNotFound());
    }

    @Test
    public void deleteExhibition_ShouldReturn200() throws Exception {
        testExhibition.setArtworks(List.of(testArtwork));
        Exhibition saved = exhibitionRepository.save(testExhibition);

        mockMvc.perform(delete("/exhibitions/" + saved.getId())
                        .with(user(testUdi)))
                .andExpect(status().isNoContent());
        List<Exhibition> exhibitions = exhibitionRepository.findAll();
        assertEquals(0, exhibitions.size());
    }

    @Test
    public void deleteExhibition_ShouldReturn409_WhenConvertedIntoAuction() throws Exception {
        testExhibition.setArtworks(List.of(testArtwork));
        testExhibition.setStatus(ExhibitionStatus.converted_into_auction);
        Exhibition saved = exhibitionRepository.save(testExhibition);

        mockMvc.perform(delete("/exhibitions/" + saved.getId())
                        .with(user(testUdi)))
                .andExpect(status().isConflict());
    }

    @Test
    public void getMyExhibitions_ShouldReturn200() throws Exception {
        testExhibition.setArtworks(List.of(testArtwork));
        exhibitionRepository.save(testExhibition);

        mockMvc.perform(get("/exhibitions/my?page=0")
                        .with(user(testUdi)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.totalPages").value(1))
                .andExpect(jsonPath("$.items[0].title").value("Exhibition"));
    }

    @Test
    public void getMyExhibition_ShouldReturn200() throws Exception {
        testExhibition.setArtworks(List.of(testArtwork));
        Exhibition saved = exhibitionRepository.save(testExhibition);

        mockMvc.perform(get("/exhibitions/my/" + saved.getId())
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
