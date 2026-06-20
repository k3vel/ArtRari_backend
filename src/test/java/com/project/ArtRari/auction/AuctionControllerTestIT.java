package com.project.ArtRari.auction;

import com.project.ArtRari.artwork.Artwork;
import com.project.ArtRari.artwork.ArtworkRepository;
import com.project.ArtRari.artwork.WorkStatus;
import com.project.ArtRari.auction.dto.AuctionCreateRequest;
import com.project.ArtRari.auction.dto.AuctionUpdateRequest;
import com.project.ArtRari.exhibition.Exhibition;
import com.project.ArtRari.exhibition.ExhibitionRepository;
import com.project.ArtRari.exhibition.ExhibitionStatus;
import com.project.ArtRari.lot.Lot;
import com.project.ArtRari.lot.LotRepository;
import com.project.ArtRari.lot.LotStatus;
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
import tools.jackson.databind.json.JsonMapper;

import java.math.BigDecimal;
import java.time.Instant;
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
public class AuctionControllerTestIT {
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private JsonMapper jsonMapper;
    @Autowired
    private AuctionRepository auctionRepository;
    @Autowired
    private ExhibitionRepository exhibitionRepository;
    @Autowired
    private ArtworkRepository artworkRepository;
    @Autowired
    private LotRepository lotRepository;
    @Autowired
    private UserRepository userRepository;

    private User testUser;
    private UserDetailsImpl testUdi;
    private Exhibition testExhibition;
    private Auction testAuction;
    private Lot testLot;
    private AuctionCreateRequest auctionCreateRequest;
    private AuctionUpdateRequest auctionUpdateRequest;

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

        Artwork artwork = new Artwork();;
        artwork.setTitle("Title");
        artwork.setOwner(testUser);
        artwork.setDescription("Description");
        artwork.setTechnique("Technique");
        artwork.setStatus(WorkStatus.available);
        artwork.setAuthor("Author");
        artwork.setTags(List.of());
        artwork.setCreationDate(LocalDate.now());
        artwork.setPhotoUrl("https://photo.com");
        artwork.setStartPrice(new BigDecimal("1.1"));
        artworkRepository.save(artwork);

        testExhibition = new Exhibition();
        testExhibition.setArtworks(List.of(artwork));
        testExhibition.setCurator(testUser);
        testExhibition.setTitle("Exhibition");
        testExhibition.setTheme("Theme");
        testExhibition.setDescription("Description");
        testExhibition.setBackgroundUrl("https://photo.com");
        testExhibition.setStartDate(Instant.now());
        testExhibition.setStatus(ExhibitionStatus.converted_into_auction);
        testExhibition.setThumbnailUrl("https://photo.com");
        Exhibition savedExhibition = exhibitionRepository.save(testExhibition);

        testAuction = new Auction();
        testAuction.setExhibition(testExhibition);
        Instant now = Instant.now();
        testAuction.setStartDate(now);
        testAuction.setEndDate(now.plusSeconds(600));
        testAuction.setStep(new BigDecimal("1.0"));
        testAuction.setStatus(AuctionStatus.active);

        testLot = new Lot();
        testLot.setArtwork(artwork);
        testLot.setCurrentPrice(new BigDecimal("2.2"));
        testLot.setEndDate(now.plusSeconds(600));
        testLot.setBids(List.of());
        testLot.setStatus(LotStatus.available);

        auctionCreateRequest = new AuctionCreateRequest(savedExhibition.getId(),
                now.plusSeconds(60), new BigDecimal("1.0"), now.plusSeconds(660));
        auctionUpdateRequest = new AuctionUpdateRequest(now.plusSeconds(60), now.plusSeconds(660),
                new BigDecimal("2.0"));
    }

    @Test
    public void getAuctionsWithEmptyTags_ShouldReturn200() throws Exception {
        testAuction.setLots(List.of(testLot));
        auctionRepository.save(testAuction);
        lotRepository.save(testLot);

        mockMvc.perform(get("/auctions?page=0")
                        .with(user(testUdi)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.totalPages").value(1))
                .andExpect(jsonPath("$.items[0].title").value("Exhibition"));
    }

    @Test
    public void getAuction_ShouldReturn200() throws Exception {
        testAuction.setLots(List.of(testLot));
        Auction savedAuction = auctionRepository.save(testAuction);
        lotRepository.save(testLot);

        mockMvc.perform(get("/auctions/{id}", savedAuction.getId())
                        .with(user(testUdi)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Exhibition"))
                .andExpect(jsonPath("$.lotPreviews[0].title").value("Title"));
    }

    @Test
    public void getAuction_ShouldReturn400_WhenAuctionDoesNotExist() throws Exception {
        mockMvc.perform(get("/auctions/{id}", 1)
                        .with(user(testUdi)))
                .andExpect(status().isNotFound());
    }

    @Test
    public void createAuction_ShouldReturn201() throws Exception {
        testExhibition.setStatus(ExhibitionStatus.running);
        exhibitionRepository.save(testExhibition);
        String request = jsonMapper.writeValueAsString(auctionCreateRequest);

        mockMvc.perform(post("/auctions")
                        .with(user(testUdi))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title").value("Exhibition"))
                .andExpect(jsonPath("$.lotPreviews[0].title").value("Title"));
        List<Auction> auctions = auctionRepository.findAll();
        assertEquals(1, auctions.size());
        assertEquals("Exhibition", auctions.get(0).getExhibition().getTitle());
    }

    @Test
    void createAuction_ShouldReturn404_WhenAuctionDoesNotExist() throws Exception {
        AuctionCreateRequest wrongRequest = new AuctionCreateRequest(999L,
                Instant.now().plusSeconds(60), new BigDecimal("1.0"), Instant.now().plusSeconds(660));
        String request = jsonMapper.writeValueAsString(wrongRequest);

        mockMvc.perform(post("/auctions")
                        .with(user(testUdi))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isNotFound());
    }

    @Test
    void updateAuction_ShouldReturn200() throws Exception {
        testAuction.setLots(List.of(testLot));
        testAuction.setStartDate(Instant.now().plusSeconds(60));
        testAuction.setStatus(AuctionStatus.scheduled);
        Auction savedAuction = auctionRepository.save(testAuction);
        lotRepository.save(testLot);
        String request = jsonMapper.writeValueAsString(auctionUpdateRequest);

        mockMvc.perform(put("/auctions/" + savedAuction.getId())
                        .with(user(testUdi))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andDo(print())
                .andExpect(status().isOk());
        Auction auction = auctionRepository.findById(savedAuction.getId()).get();
        assertEquals(BigDecimal.valueOf(2.0), auction.getStep());
    }

    @Test
    void updateAuction_ShouldReturn401_WhenWrongCurator() throws Exception {
        testAuction.setLots(List.of(testLot));
        Auction savedAuction = auctionRepository.save(testAuction);
        lotRepository.save(testLot);
        User wrongUser = new User();
        wrongUser.setFirstName("Wrong");
        wrongUser.setLastName("User");
        wrongUser.setPhone("321");
        wrongUser.setEmail("cba");
        wrongUser.setPassword("qqq");
        wrongUser.setRole(Role.curator);
        wrongUser.setBanned(false);
        User savedUser = userRepository.save(wrongUser);
        UserDetailsImpl wrongUdi = new UserDetailsImpl(savedUser.getId(), "Wrong", "User", "321",
                "cba", "qqq", Role.curator, false);
        String request = jsonMapper.writeValueAsString(auctionUpdateRequest);

        mockMvc.perform(put("/auctions/" + savedAuction.getId())
                        .with(user(wrongUdi))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isForbidden());
    }

    @Test
    void updateAuction_ShouldReturn409_WhenAuctionAlreadyRunning() throws Exception {
        testAuction.setLots(List.of(testLot));
        Auction savedAuction = auctionRepository.save(testAuction);
        lotRepository.save(testLot);
        String request = jsonMapper.writeValueAsString(auctionUpdateRequest);

        mockMvc.perform(put("/auctions/" + savedAuction.getId())
                        .with(user(testUdi))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isConflict());
    }

    @Test
    void deleteAuction_ShouldReturn204() throws Exception {
        testAuction.setLots(List.of(testLot));
        testAuction.setStartDate(Instant.now().plusSeconds(60));
        testAuction.setStatus(AuctionStatus.scheduled);
        Auction savedAuction = auctionRepository.save(testAuction);
        lotRepository.save(testLot);

        mockMvc.perform(delete("/auctions/" + savedAuction.getId())
                        .with(user(testUdi)))
                .andExpect(status().isNoContent());
    }

    @Test
    void deleteAuction_ShouldReturn409_WhenAuctionAlreadyRunning() throws Exception {
        testAuction.setLots(List.of(testLot));
        Auction savedAuction = auctionRepository.save(testAuction);
        lotRepository.save(testLot);

        mockMvc.perform(delete("/auctions/" + savedAuction.getId())
                        .with(user(testUdi)))
                .andExpect(status().isConflict());
    }

    @Test
    void getMyAuctions_ShouldReturn200() throws Exception {
        testAuction.setLots(List.of(testLot));
        Auction savedAuction = auctionRepository.save(testAuction);
        lotRepository.save(testLot);

        mockMvc.perform(get("/auctions/my")
                        .with(user(testUdi)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.totalPages").value(1))
                .andExpect(jsonPath("$.items[0].title").value("Exhibition"));
    }
}
