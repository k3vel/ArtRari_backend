package com.project.ArtRari.artwork;

import tools.jackson.databind.json.JsonMapper;
import com.project.ArtRari.artwork.dto.ArtworkCreateRequest;
import com.project.ArtRari.artwork.dto.ArtworkResponse;
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
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
@Transactional
public class ArtworkControllerTest {
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private JsonMapper mapper;
    @Autowired
    private ArtworkService artworkService;
    @Autowired
    private ArtworkRepository artworkRepository;
    @Autowired
    private UserRepository userRepository;

    private User testUser;
    private UserDetailsImpl testUdi;

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
        testUdi = new UserDetailsImpl(testUser.getId(), testUser.getFirstName(), testUser.getLastName(),
                testUser.getPhone(), testUser.getEmail(), testUser.getPassword(), testUser.getRole(),
                testUser.isBanned());
    }

    @Test
    public void getArtwork_ShouldReturn200AndArtwork() throws Exception {
        Artwork artwork = new Artwork();
        artwork.setTitle("Mona Lisa");
        artwork.setOwner(testUser);
        artwork.setAuthor("Author");
        artwork.setStartPrice(new BigDecimal("0.0"));
        artwork.setTags(List.of());
        artwork.setStatus(WorkStatus.available); //todo work status on auction on exhibition ...
        Artwork savedArtwork = artworkRepository.save(artwork);

        mockMvc.perform(get("/artworks/" + savedArtwork.getId())
                .with(user(testUdi)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Mona Lisa"));

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
        ArtworkCreateRequest request = new ArtworkCreateRequest("Title", "Author", "Description",
                "Technique", List.of(), LocalDate.now(), "https://photo.com", new BigDecimal(1.1));
        String jsonRequest = mapper.writeValueAsString(request);
        mockMvc.perform(post("/artworks")
                        .with(user(testUdi))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonRequest))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title").value("Title"));
        List<Artwork> artworks = artworkRepository.findAll();
        assertEquals(1, artworks.size());
        assertEquals("Title", artworks.get(0).getTitle());
        assertEquals(testUser.getId(), artworks.get(0).getOwner().getId());
    }
}
