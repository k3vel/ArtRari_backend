package com.project.ArtRari.artwork;


import com.project.ArtRari.artwork.dto.ArtworkUpdateRequest;
import tools.jackson.databind.json.JsonMapper;
import com.project.ArtRari.artwork.dto.ArtworkCreateRequest;
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
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
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
    private ArtworkService artworkService;
    @Autowired
    private UserRepository userRepository;

    private User testUser;
    private UserDetailsImpl testUdi;
    private Artwork testArtwork;
    private String artworkCreateRequest;
    private String artworkUpdateRequest;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setFirstName("John");
        testUser.setLastName("Doe");
        testUser.setEmail("john@doe.com");
        testUser.setPassword("password");
        testUser.setPhone("123456789");
        testUser.setRole(Role.user);
        userRepository.save(testUser);

        testArtwork = new Artwork();
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

        testUdi = new UserDetailsImpl(testUser.getId(), testUser.getFirstName(), testUser.getLastName(),
                testUser.getPhone(), testUser.getEmail(), testUser.getPassword(), testUser.getRole(),
                testUser.isBanned());

        ArtworkCreateRequest createRequest = new ArtworkCreateRequest("Title", "Author", "Description",
                "Technique", List.of(), LocalDate.now(), "https://photo.com", new BigDecimal(1.1));
        artworkCreateRequest = mapper.writeValueAsString(createRequest);

        ArtworkUpdateRequest updateRequest = new ArtworkUpdateRequest("New Title", "Author",
                "Description", "Technique", List.of(), LocalDate.now(),
                "https://photo.com", new BigDecimal(1.1));
        artworkUpdateRequest = mapper.writeValueAsString(updateRequest);
    }

    @Test
    public void getArtwork_ShouldReturn200AndArtwork() throws Exception {
        Artwork savedArtwork = artworkRepository.save(testArtwork); //todo work status on auction on exhibition ...

        mockMvc.perform(get("/artworks/" + savedArtwork.getId())
                .with(user(testUdi)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Title"));

    }

    @Test
    public void getArtwork_ShouldReturn404_WhenWorkDoesNotExist() throws Exception {
        mockMvc.perform(get("/artworks/0")
                .with(user(testUdi)))
                .andExpect(status().isNotFound());
    }

    @Test
    public void addArtwork_ShouldReturn201() throws Exception {
        userRepository.save(testUser);
        mockMvc.perform(post("/artworks")
                        .with(user(testUdi))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(artworkCreateRequest))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title").value("Title"));
        List<Artwork> artworks = artworkRepository.findAll();
        assertEquals(1, artworks.size());
        assertEquals("Title", artworks.get(0).getTitle());
        assertEquals(testUser.getId(), artworks.get(0).getOwner().getId());
    }

    @Test
    public void addArtwork_ShouldReturn401_WhenUnauthorized() throws Exception {
        mockMvc.perform(post("/artworks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(artworkCreateRequest))
                .andExpect(status().isUnauthorized());
    }

    @Test
    public void updateArtwork_ShouldReturn200AndArtwork() throws Exception {
        userRepository.save(testUser);
        Artwork savedArtwork = artworkRepository.save(testArtwork);

        mockMvc.perform(put("/artworks/" + savedArtwork.getId())
                        .with(user(testUdi))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(artworkUpdateRequest))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("New Title"));
        List<Artwork> artworks = artworkRepository.findAll();
        assertEquals(1, artworks.size());
        assertEquals("New Title", artworks.get(0).getTitle());
        assertEquals(testUser.getId(), artworks.get(0).getOwner().getId());
    }

    @Test
    public void updateArtwork_ShouldReturn404_WhenWorkDoesNotExist() throws Exception {
        userRepository.save(testUser);
        mockMvc.perform(put("/artworks/1")
                        .with(user(testUdi))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(artworkUpdateRequest))
                .andExpect(status().isNotFound());
    }

    @Test
    public void updateArtwork_ShouldReturn401_WhenUnauthorized() throws Exception {
        Artwork savedArtwork = artworkRepository.save(testArtwork);
        mockMvc.perform(put("/artworks/" + savedArtwork.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(artworkUpdateRequest))
                .andExpect(status().isUnauthorized());
    }

    @Test
    public void updateArtwork_ShouldReturn403_WhenWrongRole() throws Exception {
        Artwork savedArtwork = artworkRepository.save(testArtwork);
        testUser.setRole(Role.curator);
        testUdi.setRole(Role.curator);
        userRepository.save(testUser);

        mockMvc.perform(put("/artworks/" + savedArtwork.getId())
                        .with(user(testUdi))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(artworkUpdateRequest))
                .andExpect(status().isForbidden());
    }

    @Test
    public void deleteArtwork_ShouldReturn200() throws Exception {
        Artwork savedArtwork = artworkRepository.save(testArtwork);
        userRepository.save(testUser);
        mockMvc.perform(delete("/artworks/" + savedArtwork.getId())
                        .with(user(testUdi)))
                .andExpect(status().isNoContent());
        List<Artwork> artworks = artworkRepository.findAll();
        assertEquals(0, artworks.size());
    }

    @Test
    public void deleteArtwork_ShouldReturn404_WhenWorkDoesNotExist() throws Exception {
        userRepository.save(testUser);
        mockMvc.perform(delete("/artworks/1")
                        .with(user(testUdi)))
                .andExpect(status().isNotFound());
    }

    @Test
    public void deleteArtwork_ShouldReturn401_WhenUnauthorized() throws Exception {
        Artwork savedArtwork = artworkRepository.save(testArtwork);
        mockMvc.perform(delete("/artworks/" + savedArtwork.getId()))
                .andExpect(status().isUnauthorized());
    }

    @Test
    public void deleteArtwork_ShouldReturn403_WhenWrongRole() throws Exception {
        Artwork savedArtwork = artworkRepository.save(testArtwork);
        testUser.setRole(Role.curator);
        testUdi.setRole(Role.curator);
        userRepository.save(testUser);

        mockMvc.perform(delete("/artworks/" + savedArtwork.getId())
                        .with(user(testUdi)))
                .andExpect(status().isForbidden());
    }

    @Test
    public void getAvailableArtworks_ShouldReturn200() throws Exception {
        artworkRepository.save(testArtwork);
        testUser.setRole(Role.curator);
        testUdi.setRole(Role.curator);
        userRepository.save(testUser);

        mockMvc.perform(get("/artworks?page=0")
                        .with(user(testUdi)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.totalPages").value(1))
                .andExpect(jsonPath("$.items[0].title").value("Title"));
    }

    @Test
    public void getAvailableArtworks_ShouldReturn401_WhenUnauthorized() throws Exception {
        artworkRepository.save(testArtwork);
        mockMvc.perform(get("/artworks?page=0")).andExpect(status().isUnauthorized());
    }

    @Test
    public void getAvailableArtworks_ShouldReturn403_WhenWrongRole() throws Exception {
        artworkRepository.save(testArtwork);
        userRepository.save(testUser);

        mockMvc.perform(get("/artworks?page=0")
                        .with(user(testUdi)))
                .andExpect(status().isForbidden());
    }

    @Test
    public void getAvailableArtworks_ShouldReturn200AndEmptyPage() throws Exception {
        testArtwork.setStatus(WorkStatus.sold);
        artworkRepository.save(testArtwork);
        testUser.setRole(Role.curator);
        testUdi.setRole(Role.curator);
        userRepository.save(testUser);

        mockMvc.perform(get("/artworks?page=0")
                        .with(user(testUdi)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(0))
                .andExpect(jsonPath("$.totalPages").value(0));
        List<Artwork> artworks = artworkRepository.findAll();
        assertEquals(1, artworks.size());
    }

    @Test
    public void getMyArtworks_ShouldReturn200() throws Exception {
        userRepository.save(testUser);
        artworkRepository.save(testArtwork);

        mockMvc.perform(get("/artworks/my")
                        .with(user(testUdi)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.totalPages").value(1));
    }

    @Test
    public void getMyArtwork_ShouldReturn200() throws Exception {
        userRepository.save(testUser);
        Artwork savedArtwork = artworkRepository.save(testArtwork);

        mockMvc.perform(get("/artworks/my/" + savedArtwork.getId())
                        .with(user(testUdi)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Title"));
    }

    @Test
    public void getMyArtwork_ShouldReturn404_WhenArtworkDoesNotExist() throws Exception {
        userRepository.save(testUser);
        mockMvc.perform(get("/artworks/my/1")
                        .with(user(testUdi)))
                .andExpect(status().isNotFound());
    }

    @Test
    public void getMyArtwork_ShouldReturn403_WhenNotMyWork() throws Exception {
        userRepository.save(testUser);
        User otherUser = new User();
        otherUser.setFirstName("John");
        otherUser.setLastName("Doe");
        otherUser.setPhone("123");
        otherUser.setEmail("johndoe@gmail.com");
        otherUser.setPassword("456");
        otherUser.setRole(Role.user);
        userRepository.save(otherUser);
        testArtwork.setOwner(otherUser);
        Artwork savedArtwork = artworkRepository.save(testArtwork);

        mockMvc.perform(get("/artworks/my/" + savedArtwork.getId())
                        .with(user(testUdi)))
                .andExpect(status().isForbidden());
    }
}
